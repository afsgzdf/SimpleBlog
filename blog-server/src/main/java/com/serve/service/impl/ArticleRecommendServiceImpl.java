package com.serve.service.impl;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import com.serve.es.repository.ArticleESRepository;
import com.serve.es.po.ESArticleDoc;
import com.serve.service.ArticleRecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleRecommendServiceImpl implements ArticleRecommendService {

    private final ElasticsearchOperations elasticsearchOperations;

    private final ArticleESRepository articleESRepository;

    @Override
    public List<ESArticleDoc> hotRecommend(Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(
                Sort.Order.desc("viewCount"),
                Sort.Order.desc("likeCount"),
                Sort.Order.desc("createTime")
        ));

        return articleESRepository.findAll(pageRequest).toList();
    }

    @Override
    public List<ESArticleDoc> tagRecommend(List<Long> targetTagIds, Long excludeCurrentArticleId) {
        //根据标签List查询
        TermsQuery tagIdsQuery = TermsQuery.of(tag -> tag
                .field("tagIds")
                .terms(targetTagId -> targetTagId.value(targetTagIds.stream().map(FieldValue::of).toList()))
        );

        BoolQuery.Builder articleTagsQuery = new BoolQuery.Builder()
                .must(tagIdsQuery._toQuery());

        if (excludeCurrentArticleId != null) {
            articleTagsQuery.mustNot(
                    TermQuery.of(id -> id.field("id").value(excludeCurrentArticleId))._toQuery()
            );
        }

        Query query = articleTagsQuery.build()._toQuery();

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query)
                .withPageable(PageRequest.of(0, 10))
                .build();

        SearchHits<ESArticleDoc> searchHits = elasticsearchOperations.search(nativeQuery, ESArticleDoc.class);

        return searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
    }

    @Override
    public List<ESArticleDoc> similarRecommend(String articleMsg, Long excludeCurrentArticleId) {
        //权重查询
        BoolQuery.Builder articleMsgQuery = new BoolQuery.Builder()
                .must(MultiMatchQuery.of(Msg -> Msg
                        .fields("title^3", "content^1")
                        .query(articleMsg)
                        .analyzer("ik_smart"))._toQuery()
                );

        if (excludeCurrentArticleId != null) {
            articleMsgQuery.mustNot(
                    TermQuery.of(id -> id.field("id").value(excludeCurrentArticleId))._toQuery()
            );
        }

        Query query = articleMsgQuery.build()._toQuery();

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query)
                .withPageable(PageRequest.of(0, 10))
                .build();

        SearchHits<ESArticleDoc> searchHits = elasticsearchOperations.search(nativeQuery, ESArticleDoc.class);

        return searchHits.stream().map(SearchHit::getContent).toList();
    }
}
