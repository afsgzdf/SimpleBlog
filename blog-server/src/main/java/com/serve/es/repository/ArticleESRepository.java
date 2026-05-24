package com.serve.es.repository;

import com.serve.es.po.ESArticleDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleESRepository extends ElasticsearchRepository<ESArticleDoc, Long> {
}
