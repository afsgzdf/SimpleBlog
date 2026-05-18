package com.serve.controller;

import com.serve.anno.AsyncLocalCache;
import com.serve.anno.AsyncLocalEvict;
import com.serve.dto.CategoryDTO;
import com.serve.po.Category;
import com.serve.result.Result;
import com.serve.service.CategoryService;
import com.serve.vo.ArticleCategoryLinkVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/category")
@Tag(name = "分类管理")
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "分类列表查询")
    @GetMapping("/list")
    public Result<List<Category>> queryCategoryList(@NotNull String categoryName) {
        log.info("分类列表查询");
        List<Category> categoryList = categoryService.queryCategoryList(categoryName);
        return Result.success(categoryList);
    }

    @PostMapping("/aaa")
    public Result<String> aaa(@RequestBody String categoryName) {
        return Result.success(categoryName);
    }

    @Operation(summary = "查询所有分类")
    @GetMapping("/list/all")
    @AsyncLocalCache(prefix = "category", key = "list")
    public Result<List<Category>> queryListAll() {
        log.info("查询所有分类");
        return Result.success(
                categoryService.lambdaQuery()
                        .list()
        );
    }

    @Operation(summary = "根据分类查询文章")
    @GetMapping("/queryArticle")
    public Result<List<ArticleCategoryLinkVO>> queryArticleByCategory(Long categoryId) {
        log.info("根据分类查询文章");
        List<ArticleCategoryLinkVO> articleCategoryLinkVOList = categoryService.queryArticleByCategory(categoryId);
        return Result.success(articleCategoryLinkVOList);
    }

    @Operation(summary = "新增分类")
    @PostMapping()
    @AsyncLocalEvict(prefix = "category", key = "list")
    public Result addCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("新增分类");
        categoryService.addCategory(categoryDTO);
        return Result.success();
    }

    @Operation(summary = "更改分类")
    @PutMapping
    @AsyncLocalEvict(prefix = "category", key = "list")
    public Result updateCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("更改分类");
        categoryService.updateCategory(categoryDTO);
        return Result.success();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping
    @AsyncLocalEvict(prefix = "category", key = "list")
    public Result deleteCategory(Long id) {
        log.info("删除分类");
        categoryService.removeById(id);
        return Result.success();
    }
}
