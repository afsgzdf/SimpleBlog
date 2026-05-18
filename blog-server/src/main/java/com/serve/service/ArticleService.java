package com.serve.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.serve.dto.ArticleDTO;
import com.serve.dto.ArticlePageQueryDTO;
import com.serve.po.Article;
import com.serve.result.PageResult;
import com.serve.vo.ArticleCategoryLinkVO;

public interface ArticleService extends IService<Article> {

    PageResult<ArticleCategoryLinkVO> pageQuery(ArticlePageQueryDTO articlePageQueryDTO);

    void addArticle(ArticleDTO articleDTO);

    void updateArticle(ArticleDTO articleDTO);

    void deleteById(Long id);
}
