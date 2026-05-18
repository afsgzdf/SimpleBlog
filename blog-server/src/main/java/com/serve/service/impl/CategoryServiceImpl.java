package com.serve.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.serve.dto.CategoryDTO;
import com.serve.mapper.CategoryMapper;
import com.serve.po.Category;
import com.serve.service.CategoryService;
import com.serve.vo.ArticleCategoryLinkVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public List<Category> queryCategoryList(String categoryName) {
        return lambdaQuery()
                .like(Objects.nonNull(categoryName), Category::getName, categoryName)
                .list();
    }

    @Override
    public void addCategory(CategoryDTO categoryDTO) {
        Category category = BeanUtil.copyProperties(categoryDTO, Category.class);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(category);
    }

    @Override
    public void updateCategory(CategoryDTO categoryDTO) {
        Category byId = getById(categoryDTO.getId());
        if (byId == null) {
            throw new RuntimeException("分类不存在!");
        }
        Category category = BeanUtil.copyProperties(categoryDTO, Category.class);
        Integer version = byId.getVersion();
        lambdaUpdate()
                .eq(Category::getId, category.getId())
                .eq(Category::getVersion, version)
                .set(Category::getName, category.getName())
                .set(Category::getVersion, version + 1)
                .set(Category::getUpdateTime, LocalDateTime.now())
                .update();
    }

    @Override
    public List<ArticleCategoryLinkVO> queryArticleByCategory(Long categoryId) {
        return baseMapper.queryArticleByCategory(categoryId);
    }
}
