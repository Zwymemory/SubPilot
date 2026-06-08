package com.subpilot.module.category.service.impl;

import com.subpilot.module.category.entity.CategoryEntity;
import com.subpilot.module.category.mapper.CategoryMapper;
import com.subpilot.module.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final List<String> DEFAULT_CATEGORY_NAMES = List.of(
            "AI 工具",
            "云服务",
            "影音娱乐",
            "办公软件",
            "学习课程",
            "域名网站",
            "生活服务",
            "其他"
    );

    private final CategoryMapper categoryMapper;

    @Override
    public void initializeDefaultCategories(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        for (int index = 0; index < DEFAULT_CATEGORY_NAMES.size(); index++) {
            CategoryEntity category = new CategoryEntity();
            category.setUserId(userId);
            category.setName(DEFAULT_CATEGORY_NAMES.get(index));
            category.setIcon(defaultIcon(index));
            category.setSortOrder(index + 1);
            category.setCreatedAt(now);
            category.setUpdatedAt(now);
            category.setDeleted(0);
            categoryMapper.insert(category);
        }
    }

    private String defaultIcon(int index) {
        return switch (index) {
            case 0 -> "bot";
            case 1 -> "cloud";
            case 2 -> "play-circle";
            case 3 -> "briefcase";
            case 4 -> "graduation-cap";
            case 5 -> "globe";
            case 6 -> "sparkles";
            default -> "folder";
        };
    }
}
