package com.serve.po;


import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("article_tag")
public class ArticleLabel {
    @Schema(description="")
    private Long id;
    @Schema(description="文章id")
    private Long articleId;
    @Schema(description="标签id")
    private Long tagId;
}
