package com.serve.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评论信息")
public class CommentDTO {
    @Schema(description="文章id")
    private Integer articleId;
    @Schema(description="评论内容")
    private String content;
}
