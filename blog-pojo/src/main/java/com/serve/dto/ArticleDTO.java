package com.serve.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Data
public class ArticleDTO {

    @NotNull(message="[文章id]不能为空")
    @Schema(description = "文章id")
    private Long id;

    @Size(max= 32,message="编码长度不能超过32")
    @Schema(description = "文章标题")
    @Length(max= 32,message="编码长度不能超过32")
    private String title;

    @Size(max= 256,message="编码长度不能超过256")
    @Schema(description = "文章内容")
    @Length(max= 256,message="编码长度不能超过256")
    private String content;

    @Schema(description = "文章分类id")
    private Integer categoryId;

    @Schema(description = "文章标签id")
    private List<Long> tagIds;

    @Schema(description = "作者id")
    private Long userId;

    @Min(value = 0, message = "状态不合法")
    @Max(value = 1, message = "状态不合法")
    @Schema(description = "文章状态")
    private Integer status;
}
