package com.serve.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.serve.dto.CommentDTO;
import com.serve.po.Comment;
import com.serve.vo.CommentVO;

import java.util.List;

public interface CommentService extends IService<Comment> {

    void addComment(CommentDTO commentDTO);

    List<CommentVO> getCommentsByArticleId(Long id);
}
