package com.project.InventoryMgtSys.services;

import com.project.InventoryMgtSys.dtos.CategoryDTO;
import com.project.InventoryMgtSys.dtos.Response;

public interface CategoryService {

    Response createCategory(CategoryDTO categoryDTO);

    Response getAllCategories();

    Response getCategoryById(Long id);

    Response updateCategory(Long id, CategoryDTO categoryDTO);

    Response deleteCategory(Long id);
}
