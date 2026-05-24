package com.serve.controller;

import com.serve.anno.AsyncCache;
import com.serve.anno.AsyncCacheEvict;
import com.serve.anno.RateLimitPerSecond;
import com.serve.dto.ArticleDTO;
import com.serve.dto.ArticleMsgQueryDTO;
import com.serve.dto.ArticlePageQueryDTO;
import com.serve.dto.ArticleTagQueryDTO;
import com.serve.po.Article;
import com.serve.es.po.ESArticleDoc;
import com.serve.result.PageResult;
import com.serve.result.Result;
import com.serve.service.ArticleRecommendService;
import com.serve.service.ArticleService;
import com.serve.vo.ArticleCategoryLinkVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/article")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "文章管理", description = "文章相关操作")
public class ArticleController {

    private final ArticleService articleService;

    private final ArticleRecommendService articleRecommendService;

    @Operation(summary = "热点文章查询")
    @GetMapping("/search/hot")
    public Result<List<ESArticleDoc>> hotRecommend(Integer page) {
        log.info("热点文章查询");
        Integer currentPage = Math.max(page - 1, 0);
        return Result.success(articleRecommendService.hotRecommend(currentPage, 10));
    }

    @Operation(summary = "文章标签查询")
    @PostMapping("/search/tag")
    public Result<List<ESArticleDoc>> tagRecommend(@RequestBody @Valid
                                                   ArticleTagQueryDTO articleTagQueryDTO) {
        log.info("文章标签查询");
        return Result.success(articleRecommendService.tagRecommend(
                articleTagQueryDTO.getTargetTagIds(), articleTagQueryDTO.getCurrentArticleId()
        ));
    }

    @Operation(summary = "文章信息查询搜索")
    @PostMapping("/search/msg")
    public Result<List<ESArticleDoc>> similarRecommend(@RequestBody @Valid
                                                           ArticleMsgQueryDTO articleMsgQueryDTO) {
        log.info("文章信息查询搜索");
        return Result.success(articleRecommendService.similarRecommend
                (articleMsgQueryDTO.getArticleMsg(), articleMsgQueryDTO.getCurrentArticleId()));
    }

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
