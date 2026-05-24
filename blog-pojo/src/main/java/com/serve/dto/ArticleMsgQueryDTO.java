package com.serve.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ArticleMsgQueryDTO {
    @NotBlank(message = "文章内容不能为空!")
    private String articleMsg;
    private Long currentArticleId;
}
