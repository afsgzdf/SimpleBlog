package com.serve.po;


import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("tag")
public class Label {
    @Schema(description="标签id")
    private Long id;
    @Schema(description="标签名")
    private String name;
    @Schema(description = "乐观锁版本号")
    @Version
    private Integer version;
    @Schema(description="")
    private LocalDateTime createTime;
    @Schema(description="")
    private LocalDateTime updateTime;
}
