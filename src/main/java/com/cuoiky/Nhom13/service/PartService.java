package com.cuoiky.Nhom13.service;

import com.cuoiky.Nhom13.dto.PartLookupResponse;
import com.cuoiky.Nhom13.dto.PartRequest;
import com.cuoiky.Nhom13.dto.PartResponse;
import com.cuoiky.Nhom13.model.Part;
import com.cuoiky.Nhom13.repository.PartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class PartService {
    private final PartRepository partRepository;
    private final NotificationService notificationService;

    public PartService(PartRepository partRepository, NotificationService notificationService) {
        this.partRepository = partRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<PartResponse> listAll() {
        return partRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public PartResponse create(PartRequest request) {
        String partCode = request.getPartCode().trim();
        String effectiveBarcode = resolveBarcode(partCode, request.getBarcode());
        partRepository.findByPartCode(partCode)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Part code already exists");
                });
        validateBarcode(effectiveBarcode, null);

        Part part = new Part();
        applyRequest(part, request);
        part.setPartCode(partCode);
        part.setBarcode(effectiveBarcode);
        Part saved = partRepository.save(part);
        notificationService.notifyLowStockIfNeeded(saved);
        return toResponse(saved);
    }

    public PartResponse update(Long partId, PartRequest request) {
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new IllegalArgumentException("Part not found"));
        String partCode = request.getPartCode().trim();
        String effectiveBarcode = resolveBarcode(partCode, request.getBarcode());

        partRepository.findByPartCode(partCode)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(partId)) {
                        throw new IllegalArgumentException("Part code already exists");
                    }
                });
        validateBarcode(effectiveBarcode, partId);

        applyRequest(part, request);
        part.setPartCode(partCode);
        part.setBarcode(effectiveBarcode);
        Part saved = partRepository.save(part);
        notificationService.notifyLowStockIfNeeded(saved);
        return toResponse(saved);
    }

    public PartResponse adjustStock(Long partId, int deltaQuantity) {
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new IllegalArgumentException("Part not found"));
        int after = part.getStockQuantity() + deltaQuantity;
        if (after < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        part.setStockQuantity(after);
        Part saved = partRepository.save(part);
        notificationService.notifyLowStockIfNeeded(saved);
        return toResponse(saved);
    }

    public void delete(Long partId) {
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new IllegalArgumentException("Part not found"));
        partRepository.delete(part);
    }

    @Transactional(readOnly = true)
    public PartLookupResponse lookupByCode(String code) {
        String normalized = normalize(code);
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException("Lookup code is required");
        }
        Part part = partRepository.findByPartCode(normalized)
                .or(() -> partRepository.findByBarcode(normalized))
                .or(() -> resolveDerivedBarcodeLookup(normalized))
                .orElseThrow(() -> new IllegalArgumentException("Part not found for code: " + normalized));
        return toLookupResponse(part);
    }

    @Transactional(readOnly = true)
    public List<PartLookupResponse> search(String keyword) {
        String normalized = normalize(keyword);
        if (!StringUtils.hasText(normalized)) {
            return List.of();
        }
        return partRepository
                .findTop10ByPartCodeContainingIgnoreCaseOrPartNameContainingIgnoreCaseOrBarcodeContainingIgnoreCaseOrderByPartNameAsc(
                        normalized, normalized, normalized)
                .stream()
                .map(this::toLookupResponse)
                .toList();
    }

    private void applyRequest(Part part, PartRequest request) {
        part.setPartName(request.getPartName().trim());
        part.setUnit(request.getUnit().trim());
        part.setStockQuantity(request.getStockQuantity());
        part.setMinimumStockLevel(request.getMinimumStockLevel());
        part.setActive(request.getActive());
        part.setStepTemplateText(joinStepTemplates(request.getStepTemplates()));
    }

    private PartResponse toResponse(Part part) {
        return PartResponse.builder()
                .id(part.getId())
                .partCode(part.getPartCode())
                .barcode(effectiveBarcode(part))
                .partName(part.getPartName())
                .unit(part.getUnit())
                .stockQuantity(part.getStockQuantity())
                .minimumStockLevel(part.getMinimumStockLevel())
                .active(part.getActive())
                .stepTemplates(parseStepTemplates(part.getStepTemplateText()))
                .createdAt(part.getCreatedAt())
                .updatedAt(part.getUpdatedAt())
                .build();
    }

    private PartLookupResponse toLookupResponse(Part part) {
        return PartLookupResponse.builder()
                .id(part.getId())
                .partCode(part.getPartCode())
                .barcode(effectiveBarcode(part))
                .partName(part.getPartName())
                .unit(part.getUnit())
                .stockQuantity(part.getStockQuantity())
                .minimumStockLevel(part.getMinimumStockLevel())
                .active(part.getActive())
                .stepTemplates(parseStepTemplates(part.getStepTemplateText()))
                .build();
    }

    private void validateBarcode(String barcode, Long currentPartId) {
        String normalized = normalize(barcode);
        if (!StringUtils.hasText(normalized)) {
            return;
        }
        partRepository.findByBarcode(normalized).ifPresent(existing -> {
            if (currentPartId == null || !existing.getId().equals(currentPartId)) {
                throw new IllegalArgumentException("Barcode already exists");
            }
        });
    }

    private String joinStepTemplates(List<String> stepTemplates) {
        if (stepTemplates == null || stepTemplates.isEmpty()) {
            return null;
        }
        List<String> normalized = stepTemplates.stream()
                .map(this::normalize)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        return normalized.isEmpty() ? null : String.join("\n", normalized);
    }

    private List<String> parseStepTemplates(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        return Arrays.stream(value.split("\\R"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String resolveBarcode(String partCode, String requestedBarcode) {
        String normalized = normalize(requestedBarcode);
        return StringUtils.hasText(normalized) ? normalized : "PART:" + partCode;
    }

    private String effectiveBarcode(Part part) {
        return StringUtils.hasText(part.getBarcode()) ? part.getBarcode() : "PART:" + part.getPartCode();
    }

    private java.util.Optional<Part> resolveDerivedBarcodeLookup(String code) {
        if (code.startsWith("PART:")) {
            return partRepository.findByPartCode(code.substring("PART:".length()));
        }
        return java.util.Optional.empty();
    }
}
