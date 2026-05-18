package com.serve.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentVO implements Serializable {
    @Schema(description="评论id")
    private Long id;
    @Schema(description="评论用户")
    private Long userId;
    @Schema(description="文章id")
    private Long articleId;
    @Schema(description="评论内容")
    private String content;
    @Schema(description="评论时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @Schema(description = "用户昵称")
    private String nickname;
    @Schema(description = "用户名")
    private String username;
}
