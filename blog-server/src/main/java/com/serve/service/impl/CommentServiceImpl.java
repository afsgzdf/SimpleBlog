package com.serve.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.serve.context.BaseContext;
import com.serve.dto.CommentDTO;
import com.serve.mapper.CommentMapper;
import com.serve.po.Comment;
import com.serve.service.ArticleService;
import com.serve.service.CommentService;
import com.serve.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final ArticleService articleService;

    @Override
    public void addComment(CommentDTO commentDTO) {
        Comment comment = BeanUtil.copyProperties(commentDTO, Comment.class);
        comment.setUserId(BaseContext.getThreadLocal());
        comment.setCreateTime(LocalDateTime.now());
        baseMapper.insert(comment);
    }

    @Override
    public List<CommentVO> getCommentsByArticleId(Long id) {
        //根据文章id查询评论列表
        List<CommentVO> commentListByArticleId = baseMapper.getCommentListByArticleId(id);
        return commentListByArticleId;
    }
}
