package com.serve.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticlePageQueryDTO {

    private Integer pageNo;
    private Integer pageSize;

    private String title;

    private boolean querySelf;

    private boolean isQueryStatus;

    private Integer categoryId;

    private LocalDateTime createTime;
}
