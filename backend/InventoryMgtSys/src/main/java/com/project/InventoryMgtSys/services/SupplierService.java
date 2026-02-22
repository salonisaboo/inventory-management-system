package com.project.InventoryMgtSys.services;

import com.project.InventoryMgtSys.dtos.Response;
import com.project.InventoryMgtSys.dtos.SupplierDTO;

public interface SupplierService {

    Response addSupplier(SupplierDTO supplierDTO);

    Response updateSupplier(Long id, SupplierDTO supplierDTO);

    Response getAllSupplier();

    Response getSupplierById(Long id);

    Response deleteSupplier(Long id);

}
