package com.subpilot.module.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Category create request")
public record CategoryCreateRequest(
        @NotBlank(message = "分类名称不能为空")
        @Size(max = 64, message = "分类名称不能超过 64 个字符")
        String name,

        @Size(max = 64, message = "图标不能超过 64 个字符")
        String icon,

        @Min(value = 0, message = "排序值不能小于 0")
        @Max(value = 9999, message = "排序值不能大于 9999")
        Integer sortOrder
) {
}
