package com.serve.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "标签必要信息")
public class LabelDTO {

    @Schema(description = "标签id")
    private Long id;

    @NotNull(message = "标签名称不能为空")
    @Schema(description="标签名")
    private String name;
}
