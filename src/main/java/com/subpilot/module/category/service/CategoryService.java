package com.subpilot.module.category.service;

import com.subpilot.module.category.dto.CategoryCreateRequest;
import com.subpilot.module.category.dto.CategoryUpdateRequest;
import com.subpilot.module.category.entity.CategoryEntity;
import com.subpilot.module.category.vo.CategoryVO;

import java.util.List;

public interface CategoryService {

    void initializeDefaultCategories(Long userId);

    List<CategoryVO> listCurrentUserCategories();

    CategoryVO create(CategoryCreateRequest request);

    CategoryVO update(Long id, CategoryUpdateRequest request);

    void delete(Long id);

    CategoryEntity getOwnedCategoryOrThrow(Long userId, Long categoryId);

    CategoryVO toVO(CategoryEntity category);
}
