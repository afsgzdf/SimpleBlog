package com.serve.dto;

import com.serve.po.AIChatMessage;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CustomCreateContextRequestDTO {
    private String model;
    private String mode;
    private List<AIChatMessage> messages;
    private Integer ttl;
}
