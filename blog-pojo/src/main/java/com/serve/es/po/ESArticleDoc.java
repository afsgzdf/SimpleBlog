package com.serve.es.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文章属性")
@Document(indexName = "article")
public class ESArticleDoc {

    /**
     * 文章id
     */
    @Id
    @Schema(description = "文章id")
    private Long id;
    /**
     * 文章标题
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;
    /**
     * 文章内容
     */
    @Size(max= 256,message="编码长度不能超过256")
    @Schema(description = "文章内容")
    @Length(max= 256,message="编码长度不能超过256")
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;
    /**
     * 文章分类id
     */
    @Schema(description = "文章分类id")
    @Field(type = FieldType.Keyword)
    private Integer categoryId;

    @Schema(description = "文章标签列表")
    @Field(type = FieldType.Keyword)
    private List<Long> tagIds;

    @Schema(description = "阅读量")
    @Field(type = FieldType.Integer)
    private Integer viewCount;

    @Schema(description = "点赞数")
    @Field(type = FieldType.Integer)
    private Integer likeCount;
    /**
     * 发表时间
     */
    @Schema(description = "发表时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
