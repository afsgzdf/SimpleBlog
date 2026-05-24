package com.serve.test;

import com.serve.es.po.ESArticleDoc;
import com.serve.es.repository.ArticleESRepository;
import com.serve.mapper.ArticleMapper;
import com.serve.po.Article;
import com.serve.po.Label;
import com.serve.service.ArticleLabelService;
import com.serve.service.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class ArticleServiceTest {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private ArticleLabelService articleLabelService;
    @Autowired
    private ArticleESRepository articleESRepository;

    private ESArticleDoc convertToDoc(Article article, List<Long> tagIds) {
        return new ESArticleDoc(
                article.getId(),
                article.getTitle(),
                article.getContent(),
                article.getCategoryId(),
                tagIds,
                article.getViewCount(),
                article.getLikeCount(),
                article.getCreateTime()
        );
    }
    @Test
    void syncAllData() {
        List<Article> articles = articleMapper.selectList(null);

        List<ESArticleDoc> ESArticleList = new ArrayList<>();

        articles.forEach(article -> {
            List<Long> labelIds = articleLabelService.queryLabelIdByArticleId(article.getId());
            ESArticleList.add(convertToDoc(article, labelIds));
        });
        articleESRepository.deleteAll();
        articleESRepository.saveAll(ESArticleList);
    }
}
