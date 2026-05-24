package com.serve.service;

import com.serve.es.po.ESArticleDoc;

import java.util.List;

public interface ArticleRecommendService {

    List<ESArticleDoc> hotRecommend(Integer page, Integer size);

    List<ESArticleDoc> tagRecommend(List<Long> targetTagIds, Long excludeCurrentArticle);

    List<ESArticleDoc> similarRecommend(String articleMsg, Long excludeCurrentArticle);
}
