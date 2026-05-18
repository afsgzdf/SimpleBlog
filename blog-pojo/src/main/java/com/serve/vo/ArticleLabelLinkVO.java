package com.serve.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleLabelLinkVO {

    /**
     * 文章id
     */
    @NotNull(message="[文章id]不能为空")
    @Schema(description = "文章id")
    private Long id;
    /**
     * 文章标题
     */
    @Size(max= 32,message="编码长度不能超过32")
    @Schema(description = "文章标题")
    @Length(max= 32,message="编码长度不能超过32")
    private String title;
    /**
     * 文章内容
     */
    @Size(max= 256,message="编码长度不能超过256")
    @Schema(description = "文章内容")
    @Length(max= 256,message="编码长度不能超过256")
    private String content;
    /**
     * 文章分类id
     */
    @Schema(description = "文章分类id")
    private Integer categoryId;
    /**
     * 作者id
     */
    @Schema(description = "作者id")
    private Long userId;
    /**
     * 文章状态
     */
    @Schema(description = "文章状态")
    private Integer status;
    /**
     * 发表时间
     */
    @Schema(description = "发表时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /**
     * 更改时间
     */
    @Schema(description = "更改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 标签id
     */
    @Schema(description = "标签id")
    private List<Long> labelId;

    /**
     * 标签名称
     */
    @Schema(description = "标签名称")
    private List<String> labelName;
}
