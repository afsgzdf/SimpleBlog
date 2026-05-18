package com.serve.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.serve.context.BaseContext;
import com.serve.dto.ArticleDTO;
import com.serve.dto.ArticlePageQueryDTO;
import com.serve.exception.DeleteFailedException;
import com.serve.mapper.ArticleMapper;
import com.serve.po.Article;
import com.serve.result.PageResult;
import com.serve.service.ArticleService;
import com.serve.vo.ArticleCategoryLinkVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    private final ArticleMapper articleMapper;

    @Override
    public PageResult<ArticleCategoryLinkVO> pageQuery(ArticlePageQueryDTO articlePageQueryDTO) {
        Page<Article> page = new Page<>(articlePageQueryDTO.getPageNo(), articlePageQueryDTO.getPageSize());
        Article article = new Article();
        article.setTitle(articlePageQueryDTO.getTitle());
        article.setCategoryId(articlePageQueryDTO.getCategoryId());
        article.setCreateTime(articlePageQueryDTO.getCreateTime());

        if (articlePageQueryDTO.isQuerySelf()) {
            article.setUserId(BaseContext.getThreadLocal());
        }
        IPage<ArticleCategoryLinkVO> articlePage = articleMapper.articlePageQuery(page, article);

        return new PageResult<ArticleCategoryLinkVO>(articlePage.getTotal(), articlePage.getRecords());
    }

    @Override
    public void addArticle(ArticleDTO articleDTO) {
        Article article = BeanUtil.copyProperties(articleDTO, Article.class);
        article.setUserId(BaseContext.getThreadLocal());
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        articleMapper.insert(article);
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
                .set(Objects.nonNull(article.getContent()), Article::getContent, article.getContent())
                .set(Objects.nonNull(article.getCategoryId()), Article::getCategoryId, article.getCategoryId())
                .set(Objects.nonNull(article.getStatus()), Article::getStatus, article.getStatus())
                .set(Article::getVersion, byId.getVersion() + 1)
                .set(Article::getUpdateTime, LocalDateTime.now())
                .update();
    }

    @Override
    public void deleteById(Long id) {
        Article article = articleMapper.selectById(id);

        if (article.getUserId().equals(BaseContext.getThreadLocal())) {
            articleMapper.deleteById(id);
        }

        throw new DeleteFailedException("用户与文章作者不相同!");
    }
}
