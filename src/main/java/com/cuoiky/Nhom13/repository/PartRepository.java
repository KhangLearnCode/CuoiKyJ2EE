package com.cuoiky.Nhom13.repository;

import com.cuoiky.Nhom13.model.Part;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartRepository extends JpaRepository<Part, Long> {
    Optional<Part> findByPartCode(String partCode);
    Optional<Part> findByBarcode(String barcode);
    List<Part> findTop10ByPartCodeContainingIgnoreCaseOrPartNameContainingIgnoreCaseOrBarcodeContainingIgnoreCaseOrderByPartNameAsc(
            String partCode, String partName, String barcode);
}
