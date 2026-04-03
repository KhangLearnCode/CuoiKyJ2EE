package com.cuoiky.Nhom13.service;

import com.cuoiky.Nhom13.model.Job;
import com.cuoiky.Nhom13.model.JobChecklistItem;
import com.cuoiky.Nhom13.model.JobImage;
import com.cuoiky.Nhom13.model.JobPartUsage;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

@Service
public class JobReportService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final JobStorageService jobStorageService;

    public JobReportService(JobStorageService jobStorageService) {
        this.jobStorageService = jobStorageService;
    }

    public byte[] generate(Job job) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(outputStream);
             PdfDocument pdfDocument = new PdfDocument(writer);
             Document document = new Document(pdfDocument)) {

            document.add(new Paragraph("JOB REPORT").setBold().setFontSize(18));
            document.add(new Paragraph("Job code: " + job.getJobCode()));
            document.add(new Paragraph("Title: " + job.getTitle()));
            document.add(new Paragraph("Customer: " + job.getCustomerName()));
            document.add(new Paragraph("Address: " + job.getServiceAddress()));
            document.add(new Paragraph("Scheduled date: " + (job.getScheduledDate() != null ? job.getScheduledDate() : "")));
            document.add(new Paragraph("Status: " + job.getStatus()));
            document.add(new Paragraph("Priority: " + job.getPriority()));
            document.add(new Paragraph("Assigned user: " + (job.getAssignedUser() != null ? job.getAssignedUser().getUsername() : "Unassigned")));
            document.add(new Paragraph("Description: " + (job.getDescription() != null ? job.getDescription() : "")));

            document.add(new Paragraph("\nChecklist").setBold().setFontSize(14));
            Table checklistTable = new Table(UnitValue.createPercentArray(new float[]{4, 2, 3, 3})).useAllAvailableWidth();
            checklistTable.addHeaderCell(headerCell("Item"));
            checklistTable.addHeaderCell(headerCell("Status"));
            checklistTable.addHeaderCell(headerCell("Completed By"));
            checklistTable.addHeaderCell(headerCell("Completed At"));
            if (job.getChecklistItems().isEmpty()) {
                checklistTable.addCell(new Cell(1, 4).add(new Paragraph("No checklist items")));
            } else {
                job.getChecklistItems().stream()
                        .sorted(Comparator.comparing(JobChecklistItem::getCreatedAt))
                        .forEach(item -> {
                            checklistTable.addCell(valueCell(item.getItemName() + (item.getNote() != null && !item.getNote().isBlank() ? "\n" + item.getNote() : "")));
                            checklistTable.addCell(valueCell(Boolean.TRUE.equals(item.getCompleted()) ? "DONE" : "TODO"));
                            checklistTable.addCell(valueCell(item.getCompletedBy() != null ? item.getCompletedBy().getUsername() : ""));
                            checklistTable.addCell(valueCell(item.getCompletedAt() != null ? item.getCompletedAt().format(DATE_TIME_FORMATTER) : ""));
                        });
            }
            document.add(checklistTable);

            document.add(new Paragraph("\nParts Used").setBold().setFontSize(14));
            Table partsTable = new Table(UnitValue.createPercentArray(new float[]{2, 3, 2, 3})).useAllAvailableWidth();
            partsTable.addHeaderCell(headerCell("Code"));
            partsTable.addHeaderCell(headerCell("Name"));
            partsTable.addHeaderCell(headerCell("Qty"));
            partsTable.addHeaderCell(headerCell("Used By"));
            if (job.getPartUsages().isEmpty()) {
                partsTable.addCell(new Cell(1, 4).add(new Paragraph("No parts used")));
            } else {
                job.getPartUsages().stream()
                        .sorted((left, right) -> right.getUsedAt().compareTo(left.getUsedAt()))
                        .forEach(usage -> {
                            partsTable.addCell(valueCell(usage.getPart().getPartCode()));
                            partsTable.addCell(valueCell(usage.getPart().getPartName()));
                            partsTable.addCell(valueCell(usage.getQuantityUsed() + " " + usage.getPart().getUnit()));
                            partsTable.addCell(valueCell(usage.getUsedBy() != null ? usage.getUsedBy().getUsername() : ""));
                        });
            }
            document.add(partsTable);

            document.add(new Paragraph("\nImages").setBold().setFontSize(14));
            if (job.getImages().isEmpty()) {
                document.add(new Paragraph("No uploaded images"));
            } else {
                for (JobImage image : job.getImages().stream().sorted(Comparator.comparing(JobImage::getUploadedAt)).toList()) {
                    document.add(new Paragraph(image.getFileName() + " - " + (image.getUploadedBy() != null ? image.getUploadedBy() : "system")));
                    document.add(loadImageOrPlaceholder(image.getStoragePath(), 220, "Image unavailable"));
                }
            }

            document.add(new Paragraph("\nElectronic Signature").setBold().setFontSize(14));
            if (job.getSignaturePath() == null || job.getSignaturePath().isBlank()) {
                document.add(new Paragraph("No signature"));
            } else {
                document.add(new Paragraph("Signed by: " + (job.getSignatureSignedBy() != null ? job.getSignatureSignedBy() : "")));
                document.add(new Paragraph("Signed at: " + (job.getSignatureSignedAt() != null ? job.getSignatureSignedAt().format(DATE_TIME_FORMATTER) : "")));
                document.add(loadImageOrPlaceholder(job.getSignaturePath(), 180, "Signature image unavailable"));
            }

            document.close();
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to generate PDF report", ex);
        }
    }

    private Paragraph loadImageOrPlaceholder(String storagePath, float maxHeight, String fallbackText) {
        try {
            Resource resource = jobStorageService.loadAsResource(storagePath);
            byte[] bytes = resource.getInputStream().readAllBytes();
            Image image = new Image(ImageDataFactory.create(bytes));
            image.setAutoScale(true);
            image.setMaxHeight(maxHeight);
            return new Paragraph().add(image);
        } catch (IOException | IllegalArgumentException ex) {
            return new Paragraph(fallbackText).setItalic();
        }
    }

    private Cell headerCell(String value) {
        return new Cell().add(new Paragraph(value).setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY);
    }

    private Cell valueCell(String value) {
        return new Cell().add(new Paragraph(value != null ? value : ""));
    }
}
