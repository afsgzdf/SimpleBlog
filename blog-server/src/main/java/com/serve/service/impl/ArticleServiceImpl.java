package com.serve.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.serve.context.BaseContext;
import com.serve.dto.ArticleDTO;
import com.serve.dto.ArticlePageQueryDTO;
import com.serve.es.repository.ArticleESRepository;
import com.serve.exception.DeleteFailedException;
import com.serve.mapper.ArticleMapper;
import com.serve.po.Article;
import com.serve.es.po.ESArticleDoc;
import com.serve.po.ArticleLabel;
import com.serve.po.Label;
import com.serve.result.PageResult;
import com.serve.service.ArticleLabelService;
import com.serve.service.ArticleService;
import com.serve.vo.ArticleCategoryLinkVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    private final ArticleMapper articleMapper;

    private final ArticleESRepository articleESRepository;

    private final ArticleLabelService articleLabelService;

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

    @Override
    public PageResult<ArticleCategoryLinkVO> pageQuery(ArticlePageQueryDTO articlePageQueryDTO) {
        Page<Article> page = new Page<>(articlePageQueryDTO.getPageNo(), articlePageQueryDTO.getPageSize());
        Article article = new Article();
        article.setTitle(articlePageQueryDTO.getTitle());
        article.setCategoryId(articlePageQueryDTO.getCategoryId());
        article.setCreateTime(articlePageQueryDTO.getCreateTime());

        if (articlePageQueryDTO.isQueryStatus()) {
            article.setStatus(1);
        }

        if (articlePageQueryDTO.isQuerySelf()) {
            article.setUserId(BaseContext.getThreadLocal());
        }
        IPage<ArticleCategoryLinkVO> articlePage = articleMapper.articlePageQuery(page, article);

        return new PageResult<>(articlePage.getTotal(), articlePage.getRecords());
    }

    @Transactional
    @Override
    public void addArticle(ArticleDTO articleDTO) {
        Article article = BeanUtil.copyProperties(articleDTO, Article.class);
        article.setUserId(BaseContext.getThreadLocal());
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());

        ArticleLabel articleLabel = new ArticleLabel();
        articleLabel.setArticleId(article.getId());

        List<ArticleLabel> articleLabelList = new ArrayList<>();
        List<Long> tagIds = new ArrayList<>();

        articleDTO.getTagIds().forEach(tagId -> {
            articleLabel.setTagId(tagId);
            tagIds.add(tagId);
            articleLabelList.add(articleLabel);
        });

        insertArticle(article, tagIds);

        articleLabelService.saveBatch(articleLabelList);
    }

    @Override
    public void updateArticle(ArticleDTO articleDTO) {
        Article byId = getById(articleDTO.getId());
        if (byId == null) {
            throw new RuntimeException("文章不存在!");
        }
        Article article = BeanUtil.copyProperties(articleDTO, Article.class);
        lambdaUpdate()
                .eq(Article::getId, article.getId())
                .eq(Article::getUserId, BaseContext.getThreadLocal())
                .eq(Article::getVersion, byId.getVersion())
                .set(Objects.nonNull(article.getTitle()), Article::getTitle, article.getTitle())
                .set(Objects.nonNull(article.getCategoryId()), Article::getCategoryId, article.getCategoryId())
                .set(Objects.nonNull(article.getStatus()), Article::getStatus, article.getStatus())
                .set(Article::getVersion, byId.getVersion() + 1)
                .set(Article::getUpdateTime, LocalDateTime.now())
                .update();

        List<Label> labels = articleLabelService.queryLabelByArticleId(byId.getId());

        articleESRepository.save(convertToDoc(article, labels.stream().map(Label::getId).toList()));
    }

    @Override
    public void deleteById(Long id) {
        Article article = articleMapper.selectById(id);

        if (article.getUserId().equals(BaseContext.getThreadLocal())) {
            deleteArticleById(id);
        }

        throw new DeleteFailedException("用户与文章作者不相同!");
    }

    private void insertArticle(Article article, List<Long> tagIds) {
        articleMapper.insert(article);
        articleESRepository.save(convertToDoc(article, tagIds));
    }

    private void deleteArticleById(Long id) {
        articleMapper.deleteById(id);
        articleESRepository.deleteById(id);
    }
}
