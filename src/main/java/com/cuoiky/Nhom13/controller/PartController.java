package com.cuoiky.Nhom13.controller;

import com.cuoiky.Nhom13.dto.PartLookupResponse;
import com.cuoiky.Nhom13.dto.PartRequest;
import com.cuoiky.Nhom13.dto.PartResponse;
import com.cuoiky.Nhom13.dto.PartStockAdjustRequest;
import com.cuoiky.Nhom13.service.PartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/parts")
public class PartController {
    private final PartService partService;

    public PartController(PartService partService) {
        this.partService = partService;
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<PartResponse>> listParts() {
        return ResponseEntity.ok(partService.listAll());
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PartLookupResponse> lookupPart(@RequestParam String code) {
        return ResponseEntity.ok(partService.lookupByCode(code));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<PartLookupResponse>> searchParts(@RequestParam String keyword) {
        return ResponseEntity.ok(partService.search(keyword));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartResponse> createPart(@Valid @RequestBody PartRequest request) {
        return ResponseEntity.ok(partService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartResponse> updatePart(@PathVariable Long id, @Valid @RequestBody PartRequest request) {
        return ResponseEntity.ok(partService.update(id, request));
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartResponse> adjustStock(@PathVariable Long id, @Valid @RequestBody PartStockAdjustRequest request) {
        return ResponseEntity.ok(partService.adjustStock(id, request.getDeltaQuantity()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePart(@PathVariable Long id) {
        partService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
