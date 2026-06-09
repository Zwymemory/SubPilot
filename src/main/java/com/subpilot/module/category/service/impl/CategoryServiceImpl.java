package com.subpilot.module.category.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.subpilot.common.exception.BusinessException;
import com.subpilot.common.exception.ErrorCode;
import com.subpilot.infrastructure.redis.CacheService;
import com.subpilot.module.category.dto.CategoryCreateRequest;
import com.subpilot.module.category.dto.CategoryUpdateRequest;
import com.subpilot.module.category.entity.CategoryEntity;
import com.subpilot.module.category.mapper.CategoryMapper;
import com.subpilot.module.category.service.CategoryService;
import com.subpilot.module.category.vo.CategoryVO;
import com.subpilot.module.subscription.entity.SubscriptionEntity;
import com.subpilot.module.subscription.mapper.SubscriptionMapper;
import com.subpilot.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final SubscriptionMapper subscriptionMapper;
    private final CacheService cacheService;

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

    @Override
    public List<CategoryVO> listCurrentUserCategories() {
        Long userId = UserContext.getUserId();
        List<CategoryEntity> categories = categoryMapper.selectList(new LambdaQueryWrapper<CategoryEntity>()
                .eq(CategoryEntity::getUserId, userId)
                .orderByAsc(CategoryEntity::getSortOrder)
                .orderByAsc(CategoryEntity::getCreatedAt));
        return categories.stream().map(this::toVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryVO create(CategoryCreateRequest request) {
        Long userId = UserContext.getUserId();
        String name = normalizeName(request.name());
        ensureNameAvailable(userId, name, null);

        LocalDateTime now = LocalDateTime.now();
        CategoryEntity category = new CategoryEntity();
        category.setUserId(userId);
        category.setName(name);
        category.setIcon(request.icon());
        category.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        category.setCreatedAt(now);
        category.setUpdatedAt(now);
        category.setDeleted(0);
        categoryMapper.insert(category);
        cacheService.evictCategoryList(userId);
        return toVO(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryVO update(Long id, CategoryUpdateRequest request) {
        Long userId = UserContext.getUserId();
        getOwnedCategoryOrThrow(userId, id);
        String name = normalizeName(request.name());
        ensureNameAvailable(userId, name, id);

        categoryMapper.update(null, new LambdaUpdateWrapper<CategoryEntity>()
                .eq(CategoryEntity::getId, id)
                .eq(CategoryEntity::getUserId, userId)
                .set(CategoryEntity::getName, name)
                .set(CategoryEntity::getIcon, request.icon())
                .set(CategoryEntity::getSortOrder, request.sortOrder() == null ? 0 : request.sortOrder())
                .set(CategoryEntity::getUpdatedAt, LocalDateTime.now()));
        cacheService.evictCategoryList(userId);
        return toVO(getOwnedCategoryOrThrow(userId, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long userId = UserContext.getUserId();
        getOwnedCategoryOrThrow(userId, id);
        Long subscriptionCount = subscriptionMapper.selectCount(new LambdaQueryWrapper<SubscriptionEntity>()
                .eq(SubscriptionEntity::getUserId, userId)
                .eq(SubscriptionEntity::getCategoryId, id));
        if (subscriptionCount > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "该分类下存在订阅，不能删除");
        }
        categoryMapper.deleteById(id);
        cacheService.evictCategoryList(userId);
    }

    @Override
    public CategoryEntity getOwnedCategoryOrThrow(Long userId, Long categoryId) {
        CategoryEntity category = categoryMapper.selectOne(new LambdaQueryWrapper<CategoryEntity>()
                .eq(CategoryEntity::getId, categoryId)
                .eq(CategoryEntity::getUserId, userId)
                .last("LIMIT 1"));
        if (category == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "分类不存在");
        }
        return category;
    }

    @Override
    public CategoryVO toVO(CategoryEntity category) {
        Long subscriptionCount = subscriptionMapper.selectCount(new LambdaQueryWrapper<SubscriptionEntity>()
                .eq(SubscriptionEntity::getUserId, category.getUserId())
                .eq(SubscriptionEntity::getCategoryId, category.getId()));
        return new CategoryVO(
                category.getId(),
                category.getName(),
                category.getIcon(),
                category.getSortOrder(),
                subscriptionCount,
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    private void ensureNameAvailable(Long userId, String name, Long excludeId) {
        LambdaQueryWrapper<CategoryEntity> wrapper = new LambdaQueryWrapper<CategoryEntity>()
                .eq(CategoryEntity::getUserId, userId)
                .eq(CategoryEntity::getName, name);
        if (excludeId != null) {
            wrapper.ne(CategoryEntity::getId, excludeId);
        }
        if (categoryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "分类名称已存在");
        }
    }

    private String normalizeName(String name) {
        return name.trim();
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
