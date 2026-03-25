package com.cuoiky.Nhom13.service;

import com.cuoiky.Nhom13.dto.PartRequest;
import com.cuoiky.Nhom13.dto.PartResponse;
import com.cuoiky.Nhom13.model.Part;
import com.cuoiky.Nhom13.repository.PartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PartService {
    private final PartRepository partRepository;

    public PartService(PartRepository partRepository) {
        this.partRepository = partRepository;
    }

    @Transactional(readOnly = true)
    public List<PartResponse> listAll() {
        return partRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public PartResponse create(PartRequest request) {
        partRepository.findByPartCode(request.getPartCode().trim())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Part code already exists");
                });

        Part part = new Part();
        applyRequest(part, request);
        part.setPartCode(request.getPartCode().trim());
        return toResponse(partRepository.save(part));
    }

    public PartResponse update(Long partId, PartRequest request) {
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new IllegalArgumentException("Part not found"));

        partRepository.findByPartCode(request.getPartCode().trim())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(partId)) {
                        throw new IllegalArgumentException("Part code already exists");
                    }
                });

        applyRequest(part, request);
        part.setPartCode(request.getPartCode().trim());
        return toResponse(partRepository.save(part));
    }

    public PartResponse adjustStock(Long partId, int deltaQuantity) {
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new IllegalArgumentException("Part not found"));
        int after = part.getStockQuantity() + deltaQuantity;
        if (after < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        part.setStockQuantity(after);
        return toResponse(partRepository.save(part));
    }

    public void delete(Long partId) {
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new IllegalArgumentException("Part not found"));
        partRepository.delete(part);
    }

    private void applyRequest(Part part, PartRequest request) {
        part.setPartName(request.getPartName().trim());
        part.setUnit(request.getUnit().trim());
        part.setStockQuantity(request.getStockQuantity());
        part.setMinimumStockLevel(request.getMinimumStockLevel());
        part.setActive(request.getActive());
    }

    private PartResponse toResponse(Part part) {
        return PartResponse.builder()
                .id(part.getId())
                .partCode(part.getPartCode())
                .partName(part.getPartName())
                .unit(part.getUnit())
                .stockQuantity(part.getStockQuantity())
                .minimumStockLevel(part.getMinimumStockLevel())
                .active(part.getActive())
                .createdAt(part.getCreatedAt())
                .updatedAt(part.getUpdatedAt())
                .build();
    }
}
