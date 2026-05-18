package com.serve.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.serve.dto.LabelDTO;
import com.serve.po.Label;
import com.serve.vo.ArticleLabelLinkVO;

import java.util.List;

public interface LabelService extends IService<Label> {

    void addLabel(LabelDTO labelDTO);

    void updateLabel(LabelDTO labelDTO);

    List<ArticleLabelLinkVO> queryArticleByLabel(LabelDTO labelDTO);
}
