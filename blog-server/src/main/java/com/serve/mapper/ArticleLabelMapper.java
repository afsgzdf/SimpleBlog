package com.serve.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.serve.po.ArticleLabel;
import com.serve.po.Label;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ArticleLabelMapper extends BaseMapper<ArticleLabel> {

    List<Label> queryLabelByArticleId(Long articleId);
}
