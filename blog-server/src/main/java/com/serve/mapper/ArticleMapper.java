package com.serve.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.serve.po.Article;
import com.serve.vo.ArticleCategoryLinkVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    IPage<ArticleCategoryLinkVO> articlePageQuery(Page<Article> page, Article article);
}
