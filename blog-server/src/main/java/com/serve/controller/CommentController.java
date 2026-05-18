package com.serve.controller;

import com.serve.anno.AsyncCache;
import com.serve.anno.AsyncCacheEvict;
import com.serve.context.BaseContext;
import com.serve.dto.CommentDTO;
import com.serve.po.Comment;
import com.serve.result.Result;
import com.serve.service.CommentService;
import com.serve.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "评论管理")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "根据文章 ID 查询评论列表")
    @GetMapping("/articleId/{id}")
    @AsyncCache(prefix = "articleComment", key = "#id")
    public Result<List<CommentVO>> getCommentsByArticleId(@PathVariable Long id) {
        log.info("根据文章 ID 查询评论列表");
        return Result.success(commentService.getCommentsByArticleId(id));
    }

    @Operation(summary = "发布评论")
    @PostMapping
    @AsyncCacheEvict(prefix = "articleComment", variable = "articleId")
    public Result addComment(@RequestBody CommentDTO commentDTO) {
        log.info("发布评论");
        commentService.addComment(commentDTO);
        return Result.success();
    }

    @Operation(summary = "删除评论")
    @DeleteMapping
    @AsyncCacheEvict(prefix = "articleComment")
    public Result deleteComment(Long articleId, Long commentId) {
        log.info("删除评论");
        Comment one = commentService.lambdaQuery()
                .eq(Comment::getId, commentId)
                .eq(Comment::getUserId, BaseContext.getThreadLocal())
                .eq(Comment::getArticleId, articleId)
                .one();

        if (one != null) {
            commentService.removeById(commentId);

            return Result.success();
        }

        return Result.error("不存在该评论!");
    }
}
