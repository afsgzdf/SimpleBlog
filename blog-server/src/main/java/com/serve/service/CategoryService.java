package com.serve.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.serve.dto.CategoryDTO;
import com.serve.po.Category;
import com.serve.vo.ArticleCategoryLinkVO;

import java.util.List;

public interface CategoryService extends IService<Category> {

    List<Category> queryCategoryList(String categoryName);

    void addCategory(CategoryDTO categoryDTO);

    void updateCategory(CategoryDTO categoryDTO);

    List<ArticleCategoryLinkVO> queryArticleByCategory(Long categoryId);
}
