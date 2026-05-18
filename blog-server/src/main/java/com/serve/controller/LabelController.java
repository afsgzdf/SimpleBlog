package com.serve.controller;

import com.serve.anno.AsyncLocalCache;
import com.serve.anno.AsyncLocalEvict;
import com.serve.dto.LabelDTO;
import com.serve.po.Label;
import com.serve.result.Result;
import com.serve.service.LabelService;
import com.serve.vo.ArticleLabelLinkVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/label")
@Slf4j
@Tag(name = "标签管理")
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @Operation(summary = "根据标签信息查询文章")
    @Parameters(value = {
            @Parameter(name = "id", description = "标签id", in = ParameterIn.QUERY),
            @Parameter(name = "name", description = "标签名称", in = ParameterIn.QUERY)
    })
    @GetMapping("/queryArticle")
    public Result<List<ArticleLabelLinkVO>> articleQueryByLabel(@ModelAttribute @Valid LabelDTO labelDTO) {
        log.info("根据标签查询文章");
        return Result.success(labelService.queryArticleByLabel(labelDTO));
    }

    @Operation(summary = "查询所有标签")
    @GetMapping
    @AsyncLocalCache(prefix = "category", key = "list")
    public Result<List<Label>> queryAllLabel() {
        log.info("查询所有标签");
        return Result.success(
                labelService.list()
        );
    }

    @Operation(summary = "添加标签")
    @PostMapping("/insert")
    @AsyncLocalEvict(prefix = "category", key = "list")
    public Result addLabel(@RequestBody LabelDTO labelDTO) {
        log.info("添加标签");
        labelService.addLabel(labelDTO);
        return Result.success();
    }

    @Operation(summary = "更改标签")
    @PutMapping
    @AsyncLocalEvict(prefix = "category", key = "list")
    public Result updateLabel(@RequestBody LabelDTO labelDTO) {
        log.info("更改标签");
        labelService.updateLabel(labelDTO);
        return Result.success();
    }

    @Operation(summary = "删除标签")
    @DeleteMapping
    @AsyncLocalEvict(prefix = "category", key = "list")
    public Result deleteLabel(Long labelId) {
        log.info("删除标签");
        labelService.removeById(labelId);
        return Result.success();
    }
}
