package com.serve.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ArticleTagQueryDTO {
    @NotBlank(message = "标签列表不能为空!")
    private List<Long> targetTagIds;
    private Long currentArticleId;
}
