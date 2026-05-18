package com.serve.po;


import java.time.LocalDateTime;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Schema(description="评论id")
    private Long id;
    @Schema(description="评论用户")
    private Long userId;
    @Schema(description="文章id")
    private Long articleId;
    @Schema(description="评论内容")
    private String content;
    @Schema(description="评论时间")
    private LocalDateTime createTime;
}
