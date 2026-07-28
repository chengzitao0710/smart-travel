package com.smarttravel.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
@Schema(description = "旅行者")
@TableName("tb_traveler")
public class Traveler {
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "密码")
    private String password;
    @Schema(description = "昵称")
    private String nickName;
    @Schema(description = "头像URL")
    private String icon;
    @Schema(description = "Token版本号")
    private Integer tokenVersion;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}