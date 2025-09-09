package com.example.medicalinventory.service;

import com.example.medicalinventory.DTO.BoxRequest;
import com.example.medicalinventory.DTO.BoxStatusRequest;
import com.example.medicalinventory.DTO.ReturnRequest;
import com.example.medicalinventory.model.*;
import com.example.medicalinventory.repository.BoxImageRepository;
import com.example.medicalinventory.repository.BoxRepository;
import com.example.medicalinventory.repository.InstrumentRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.oned.Code39Writer;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoxService {

    private final BoxRepository boxRepository;
    private final BoxImageRepository boxImageRepository;
    private final FileUploadService fileUploadService;
    private final InstrumentRepository instrumentRepository;
    private final InstrumentBoxHistoryService instrumentBoxHistoryService;
    @Transactional
    public byte[] createBoxAndGeneratePdf(BoxRequest request) throws Exception {
        Box box = new Box();
        box.setBarcode(generateBarcode());
        box.setName(request.getName());
        box.setDoctorName(request.getDoctorName());
        box.setStatus(BoxStatus.CREATED);
        box.setInstruments(new ArrayList<>());
        box.setCreatedAt(LocalDateTime.now());
        box.setReturnBy(request.getReturnDate());

        Box savedBox = boxRepository.save(box);

        if (request.getInstrumentBarcodes() != null && !request.getInstrumentBarcodes().isEmpty()) {
            for (String instrumentBarcode : request.getInstrumentBarcodes()) {
                Instrument instrument = instrumentRepository.findByBarcode(instrumentBarcode)
                        .orElseThrow(() -> new RuntimeException("Instrument not found: " + instrumentBarcode));

                instrument.setStatus(InstrumentStatus.IN_BOX);
                instrumentRepository.save(instrument);

                savedBox.getInstruments().add(instrument);
                instrumentBoxHistoryService.logOperation(savedBox, instrument, HistoryOperation.ISSUED);
            }
            boxRepository.save(savedBox);
        }

        return generatePdfWithBarcode(savedBox);
    }

    private byte[] generatePdfWithBarcode(Box box) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        InputStream fontStream = getClass().getResourceAsStream("/fonts/FreeSans.ttf");
        byte[] fontBytes = fontStream.readAllBytes();
        PdfFont font = PdfFontFactory.createFont(
                FontProgramFactory.createFont(fontBytes),
                PdfEncodings.IDENTITY_H,
                PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED
        );
        document.setFont(font);
        document.add(new Paragraph("Бокс: " + box.getName()));
        if (box.getDoctorName() != null) {
            document.add(new Paragraph("Доктор: " + box.getDoctorName()));
        }
        if (box.getCreatedAt() != null) {
            document.add(new Paragraph("Создан: " + box.getCreatedAt().toLocalDate()));
        }
        if (box.getReturnBy() != null) {
            document.add(new Paragraph("Вернуть до: " + box.getReturnBy()));
        }

        Image barcodeImage = new Image(generateBarcodeImage(box.getBarcode()));
        document.add(barcodeImage);

        document.close();
        return baos.toByteArray();
    }



    private ImageData generateBarcodeImage(String code) throws WriterException {
        // Используем Code128Writer для поддержки EAN-128
        Code128Writer writer = new Code128Writer();
        BitMatrix bitMatrix = writer.encode(code, BarcodeFormat.CODE_128, 300, 100);

        try {
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            return ImageDataFactory.create(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при генерации изображения штрих-кода", e);
        }
    }

    // Генерация самого кода EAN-128C (6 символов, буквы и цифры)
    private String generateBarcode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int idx = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(idx));
        }
        return sb.toString();
    }

    @Transactional
    public Box updateBoxStatus(BoxStatusRequest boxStatusRequest) throws Exception {
        Box box = boxRepository.findByBarcode(boxStatusRequest.getBarcode())
                .orElseThrow(() -> new RuntimeException("Box not found"));

        final BoxStatus newStatus = boxStatusRequest.getBoxStatus();

        if (newStatus != BoxStatus.ISSUED) {
            if (box.getStatus() == null || box.getStatus() == BoxStatus.CREATED) {
                throw new IllegalStateException("Box cannot be updated from CREATED or null to " + newStatus);
            }
        }

        box.setStatus(newStatus);
        boxRepository.save(box);

        if (boxStatusRequest.getInstrumentBarcodes() != null) {
            for (String instrumentBarcode : boxStatusRequest.getInstrumentBarcodes()) {
                instrumentRepository.findByBarcode(instrumentBarcode)
                        .ifPresentOrElse(instrument -> {
                            if (newStatus == BoxStatus.ISSUED) {
                                instrument.setStatus(InstrumentStatus.IN_USE);
                                instrumentRepository.save(instrument);
                                instrumentBoxHistoryService.logOperation(
                                        box,
                                        instrument,
                                        HistoryOperation.ISSUED,
                                        box.getDoctorName()
                                );
                                box.setIssuedBy(LocalDate.now());
                            } else if (newStatus == BoxStatus.RETURNED) {
                                instrument.setStatus(InstrumentStatus.ACTIVE);
                                instrumentRepository.save(instrument);
                                instrumentBoxHistoryService.logOperation(
                                        box,
                                        instrument,
                                        HistoryOperation.RETURNED,
                                        box.getDoctorName()
                                );
                            }
                        }, () -> {
                            instrumentBoxHistoryService.logOperation(
                                    box,
                                    null,
                                    HistoryOperation.LOST,
                                    box.getDoctorName()
                            );
                        });
            }
        }

        if (newStatus == BoxStatus.ISSUED) {
            instrumentBoxHistoryService.logOperation(box, null, HistoryOperation.ISSUED);
        } else if (newStatus == BoxStatus.RETURNED) {
            instrumentBoxHistoryService.logOperation(box, null, HistoryOperation.RETURNED);
        }

        return box;
    }




    @Transactional
    public Box addInstrumentToBox(String boxBarcode, String instrumentBarcode) {
        Box box = boxRepository.findByBarcode(boxBarcode).orElseThrow(() -> new RuntimeException("Box not found"));
        if (box == null) throw new RuntimeException("Box not found: " + boxBarcode);

        Instrument instrument = instrumentRepository.findByBarcode(instrumentBarcode).orElseThrow(() -> new RuntimeException("Instrument not found"));
        if (instrument == null) throw new RuntimeException("Instrument not found: " + instrumentBarcode);

        instrument.setStatus(InstrumentStatus.IN_BOX);
        instrumentRepository.save(instrument);

        box.getInstruments().add(instrument);
        Box savedBox = boxRepository.save(box);
        instrumentBoxHistoryService.logOperation(savedBox, instrument, HistoryOperation.ISSUED);


        return savedBox;
    }

    @Transactional
    public Box updateBox(String boxBarcode, BoxRequest request, BoxStatus boxStatus) throws Exception {
        Box box = boxRepository.findByBarcode(boxBarcode).orElseThrow(() -> new RuntimeException("Box not found"));
        if (box == null) throw new RuntimeException("Box not found: " + boxBarcode);

        if (boxStatus != null) box.setStatus(boxStatus);
        box.setUpdatedAt(LocalDateTime.now());


        return boxRepository.save(box);
    }



    @Transactional
    public void returnBox(ReturnRequest request) {
        Box box = boxRepository.findByBarcode(request.getBoxBarcode())
                .orElseThrow(() -> new RuntimeException("Box not found"));

        for (String instrumentBarcode : request.getInstrumentBarcodes()) {
            instrumentRepository.findByBarcode(instrumentBarcode)
                    .ifPresentOrElse(instrument -> {
                        instrument.setStatus(InstrumentStatus.ACTIVE);
                        instrumentRepository.save(instrument);
                        instrumentBoxHistoryService.logOperation(box, instrument, HistoryOperation.RETURNED);
                    }, () -> {
                        // если инструмент не найден в системе
                        instrumentBoxHistoryService.logOperation(box, null, HistoryOperation.LOST);
                    });
        }

        box.setStatus(BoxStatus.RETURNED);
        boxRepository.save(box);
        instrumentBoxHistoryService.logOperation(box, null, HistoryOperation.RETURNED);
    }

    public Page<Box> getBoxesByStatus(BoxStatus status, Pageable pageable) {
        return boxRepository.findAllByStatus(status, pageable);
    }

    public Page<Box> getBoxesByReturnDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable){
        return boxRepository.findByReturnByBetween(startDate, endDate, pageable);
    };

    public Optional<Box> findByBarcode(String barcode) {
        return boxRepository.findByBarcode(barcode);
    }
}
