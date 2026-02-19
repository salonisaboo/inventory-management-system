package com.project.InventoryMgtSys.repositories;

import com.project.InventoryMgtSys.models.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}
