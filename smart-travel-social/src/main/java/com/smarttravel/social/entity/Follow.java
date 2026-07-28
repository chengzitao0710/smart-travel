package com.smarttravel.social.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "关注")
@TableName("tb_follow")
public class Follow {
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "被关注用户ID")
    private Long followUserId;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}