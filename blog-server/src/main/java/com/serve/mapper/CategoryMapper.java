package com.serve.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.serve.po.Category;
import com.serve.vo.ArticleCategoryLinkVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    List<ArticleCategoryLinkVO> queryArticleByCategory(Long categoryId);
}
