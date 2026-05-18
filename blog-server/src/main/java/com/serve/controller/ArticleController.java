package com.serve.controller;

import com.serve.anno.AsyncCache;
import com.serve.anno.AsyncCacheEvict;
import com.serve.anno.RateLimitPerSecond;
import com.serve.dto.ArticleDTO;
import com.serve.dto.ArticlePageQueryDTO;
import com.serve.po.Article;
import com.serve.result.PageResult;
import com.serve.result.Result;
import com.serve.service.ArticleService;
import com.serve.vo.ArticleCategoryLinkVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/article")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "文章管理", description = "文章相关操作")
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "文章分页查询")
    @PostMapping("/pageQuery")
    public Result<PageResult<ArticleCategoryLinkVO>> pageAndQueryArticle(@RequestBody ArticlePageQueryDTO articlePageQueryDTO) {
        log.info("文章分页和条件查询");

        return Result.success(articleService.pageQuery(articlePageQueryDTO));
    }

    @RateLimitPerSecond(permitsPerSecond = 1)
    @Operation(summary = "根据文章id查询对应文章")
    @GetMapping("/article/{id}")
    @AsyncCache(prefix = "article", key = "#id")
    public Result<Article> getArticleById(@PathVariable Long id) {
        log.info("根据文章id查询对应文章");
        return Result.success(articleService.lambdaQuery()
                    .eq(Article::getId, id)
                    .eq(Article::getStatus, 1)
                    .one());
    }

    @Operation(summary = "文章发布")
    @PostMapping()
    public Result addArticle(@RequestBody ArticleDTO articleDTO) {
        log.info("文章发布");

        articleService.addArticle(articleDTO);

        return Result.success();
    }

    @Operation(summary = "文章编辑")
    @PutMapping
    @AsyncCacheEvict(prefix = "article")
    public Result updateArticle(@RequestBody ArticleDTO articleDTO) {
        log.info("文章编辑");
        articleService.updateArticle(articleDTO);
        return Result.success();
    }

    @Operation(summary = "文章删除")
    @DeleteMapping
    @AsyncCacheEvict(prefix = "article")
    public Result deleteArticle(Long id) {
        log.info("文章删除");
        articleService.deleteById(id);
        return Result.success();
    }
}
