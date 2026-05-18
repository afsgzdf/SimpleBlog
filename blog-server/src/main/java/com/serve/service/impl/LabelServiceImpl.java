package com.serve.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.serve.dto.LabelDTO;
import com.serve.exception.LabelNotFoundException;
import com.serve.mapper.ArticleMapper;
import com.serve.mapper.LabelMapper;
import com.serve.po.Article;
import com.serve.po.Label;
import com.serve.service.ArticleLabelService;
import com.serve.service.LabelService;
import com.serve.vo.ArticleLabelLinkVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LabelServiceImpl extends ServiceImpl<LabelMapper, Label> implements LabelService {

    private final ArticleMapper articleMapper;
    private final ArticleLabelService articleLabelService;

    @Override
    public void addLabel(LabelDTO labelDTO) {
        Label label = BeanUtil.copyProperties(labelDTO, Label.class);
        label.setCreateTime(LocalDateTime.now());
        label.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(label);
    }

    @Override
    public void updateLabel(LabelDTO labelDTO) {
        Label byId = getById(labelDTO.getId());
        if (byId == null) {
            throw new RuntimeException("标签不存在!");
        }
        Label label = BeanUtil.copyProperties(labelDTO, Label.class);
        Integer version = byId.getVersion();
        lambdaUpdate()
                .set(Label::getName, label.getName())
                .set(Label::getUpdateTime, LocalDateTime.now())
                .set(Label::getVersion, version + 1)
                .eq(Label::getId, label.getId())
                .eq(Label::getVersion, version)
                .update();
    }

    @Override
    public List<ArticleLabelLinkVO> queryArticleByLabel(LabelDTO labelDTO) {
        Label label = BeanUtil.copyProperties(labelDTO, Label.class);
        //查询标签对应的文章id
        List<Long> articleIds = baseMapper.queryArticleIdByLabel(label);
        if (articleIds == null || articleIds.isEmpty()) {
            throw new LabelNotFoundException("标签不存在!");
        }
        //根据文章id查询对应文章列表
        List<Article> articles = articleMapper.selectByIds(articleIds);
        List<ArticleLabelLinkVO> articleLabelLinkVOS = BeanUtil.copyToList(articles, ArticleLabelLinkVO.class);

        for (ArticleLabelLinkVO articleLabelLinkVO : articleLabelLinkVOS) {
            List<Label> labels = articleLabelService.queryLabelByArticleId(articleLabelLinkVO.getId());
            articleLabelLinkVO.setLabelId(labels.stream().map(Label::getId).collect(Collectors.toList()));
            articleLabelLinkVO.setLabelName(labels.stream().map(Label::getName).collect(Collectors.toList()));
        }
        return articleLabelLinkVOS;
    }
}
