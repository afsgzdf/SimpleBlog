package com.serve.po;

import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
* 文章表
* @TableName article
*/
@Data
@Schema(description = "文章属性")
public class Article implements Serializable {

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

    @Schema(description = "阅读量")
    private Integer viewCount;

    @Schema(description = "点赞数")
    private Integer likeCount;

    /**
     * 乐观锁版本号
     */
    @Version
    @Schema(description = "乐观锁版本号")
    private Integer version;
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
}
