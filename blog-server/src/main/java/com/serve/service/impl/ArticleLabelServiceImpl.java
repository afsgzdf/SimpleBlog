package com.serve.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.serve.mapper.ArticleLabelMapper;
import com.serve.po.ArticleLabel;
import com.serve.po.Label;
import com.serve.service.ArticleLabelService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleLabelServiceImpl extends ServiceImpl<ArticleLabelMapper, ArticleLabel> implements ArticleLabelService {

    @Override
    public List<Label> queryLabelByArticleId(Long articleId) {
        return baseMapper.queryLabelByArticleId(articleId);
    }
}
