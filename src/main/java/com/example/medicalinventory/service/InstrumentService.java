package com.example.medicalinventory.service;

import com.example.medicalinventory.DTO.InstrumentRequest;
import com.example.medicalinventory.DTO.InstrumentReturnRequest;
import com.example.medicalinventory.model.*;
import com.example.medicalinventory.repository.InstrumentBoxHistoryRepository;
import com.example.medicalinventory.repository.InstrumentImageRepository;
import com.example.medicalinventory.repository.InstrumentRepository;
import com.example.medicalinventory.repository.InstrumentReturnRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class InstrumentService {

    private final InstrumentRepository instrumentRepository;
    private final InstrumentImageRepository instrumentImageRepository;
    private final InstrumentReturnRepository instrumentReturnRepository;
    private final FileUploadService fileUploadService;
    private final InstrumentBoxHistoryRepository historyRepository;
    private final InstrumentBoxHistoryService historyService;

    @Transactional
    public byte[] createInstrumentsAndGenerateZipSvgs(InstrumentRequest request) throws Exception {
        List<Instrument> all = new ArrayList<>();

        for (int i = 0; i < request.getQuantity(); i++) {
            Instrument instrument = Instrument.builder()
                    .name(request.getName())
                    .barcode(generateBarcode())
                    .serialNumber(generateSerialNumber())
                    .productionDate(request.getProductionDate())
                    .acceptanceDate(request.getAcceptanceDate())
                    .productionCompany(request.getProductionCompany())
                    .country(request.getCountry())
                    .composition(request.getComposition())
                    .reusable(request.getReusable())
                    .usageCount(Optional.ofNullable(request.getUsageCount()).orElse(0))
                    .status(InstrumentStatus.ACTIVE)
                    .build();

            Instrument saved = instrumentRepository.save(instrument);

            if (request.getImages() != null && !request.getImages().isEmpty()) {
                for (MultipartFile image : request.getImages()) {
                    FileEntity fileEntity = fileUploadService.saveImage(image);
                    InstrumentImage ii = InstrumentImage.builder()
                            .instrument(saved)
                            .image(fileEntity)
                            .build();
                    instrumentImageRepository.save(ii);
                }
            }

            all.add(saved);
        }

        return generateZipWithSvgs(all);
    }

    private String generateBarcode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String generateSerialNumber() {
        return "SN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private byte[] generateZipWithSvgs(List<Instrument> instruments) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos, StandardCharsets.UTF_8)) {
            for (Instrument instrument : instruments) {
                String barcode = instrument.getBarcode();

                int width = 600;
                int height = 180;
                int margin = 10;

                String svg = generateCode128Svg(barcode, width, height, margin);

                ZipEntry entry = new ZipEntry(barcode + ".svg");
                zos.putNextEntry(entry);
                zos.write(svg.getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }

    private String generateCode128Svg(String text, int width, int height, int margin) throws WriterException {
        Code128Writer writer = new Code128Writer();
        BitMatrix matrix = writer.encode(text, BarcodeFormat.CODE_128, width - margin * 2, height - margin * 2);

        int w = matrix.getWidth();
        int h = matrix.getHeight();

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"")
                .append(width)
                .append("\" height=\"")
                .append(height)
                .append("\" viewBox=\"0 0 ")
                .append(width)
                .append(" ")
                .append(height)
                .append("\">\n");

        sb.append("<rect x=\"0\" y=\"0\" width=\"").append(width).append("\" height=\"").append(height).append("\" fill=\"#FFFFFF\"/>\n");

        int offsetX = margin;
        int offsetY = margin;

        int x = 0;
        while (x < w) {
            boolean colBlack = columnHasBlack(matrix, x, h);
            if (!colBlack) {
                x++;
                continue;
            }
            int runStart = x;
            int runEnd = x + 1;
            while (runEnd < w && columnHasBlack(matrix, runEnd, h)) {
                runEnd++;
            }
            int runWidth = runEnd - runStart;

            sb.append("<rect x=\"")
                    .append(offsetX + runStart)
                    .append("\" y=\"")
                    .append(offsetY)
                    .append("\" width=\"")
                    .append(runWidth)
                    .append("\" height=\"")
                    .append(h)
                    .append("\" fill=\"#000000\"/>\n");

            x = runEnd;
        }

        sb.append("</svg>");
        return sb.toString();
    }

    private boolean columnHasBlack(BitMatrix m, int x, int h) {
        for (int y = 0; y < h; y++) {
            if (m.get(x, y)) return true;
        }
        return false;
    }

    private String escapeXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    @Transactional
    public void returnInstruments(List<String> instrumentBarcodes) {
        for (String barcode : instrumentBarcodes) {
            instrumentRepository.findByBarcode(barcode)
                    .ifPresentOrElse(instrument -> {
                        instrument.setStatus(InstrumentStatus.ACTIVE);
                        instrumentRepository.save(instrument);
                        historyService.logOperation(null, instrument, HistoryOperation.RETURNED);
                    }, () -> {
                        historyService.logOperation(null, null, HistoryOperation.LOST);
                    });
        }
    }

    @Transactional
    public byte[] returnInstrument(InstrumentReturnRequest request) throws Exception {
        Instrument instrument = instrumentRepository.findByBarcode(request.getBarcode())
                .orElseThrow(() -> new RuntimeException("Instrument not found"));

        LocalDate now = LocalDate.now();
        byte[] responseBytes = null;

        ReturnMethod method = ReturnMethod.valueOf(request.getReturnMethod().toUpperCase());
        BigDecimal paidAmount = null;

        switch (method) {
            case RETURNED:
                instrument.setStatus(InstrumentStatus.ACTIVE);
                historyService.logOperation(null, instrument, HistoryOperation.RETURNED);
                break;
            case PAID:
                if (request.getSum() == null || request.getSum().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new RuntimeException("Sum required for PAID");
                }
                instrument.setStatus(InstrumentStatus.UNACTIVE);
                paidAmount = request.getSum();
                historyService.logOperation(null, instrument, HistoryOperation.LOST);
                break;
            case REPLACED:
                instrument.setStatus(InstrumentStatus.ACTIVE);
                historyService.logOperation(null, instrument, HistoryOperation.RETURNED);
                responseBytes = generateSingleSvg(instrument);
                break;
            default:
                throw new RuntimeException("Invalid return method");
        }

        instrumentRepository.save(instrument);

        InstrumentReturn returnEntry = instrumentReturnRepository.findByInstrumentId(instrument.getId());
        if (returnEntry == null) {
            returnEntry = new InstrumentReturn();
            returnEntry.setInstrument(instrument);
        }
        returnEntry.setReturnMethod(method);
        returnEntry.setPaidAmount(paidAmount);
        returnEntry.setReturnDate(now);
        instrumentReturnRepository.save(returnEntry);

        return responseBytes;
    }

    private byte[] generateSingleSvg(Instrument instrument) throws Exception {
        String barcode = instrument.getBarcode();
        int width = 600;
        int height = 180;
        int margin = 10;
        String svg = generateCode128Svg(barcode, width, height, margin);
        return svg.getBytes(StandardCharsets.UTF_8);
    }

    public Instrument getByBarcode(String barcode) {
        return instrumentRepository.findByBarcode(barcode).orElse(null);
    }

    public Page<Instrument> findAll(Pageable pageable) {
        return instrumentRepository.findAll(pageable);
    }

    public Page<Instrument> getInstrumentsByStatus(InstrumentStatus status, Pageable pageable) {
        return instrumentRepository.findAllByStatus(status, pageable);
    }
}