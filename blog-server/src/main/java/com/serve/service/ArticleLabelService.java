package com.serve.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.serve.po.ArticleLabel;
import com.serve.po.Label;

import java.util.List;

public interface ArticleLabelService extends IService<ArticleLabel> {

    List<Label> queryLabelByArticleId(Long articleId);
}
