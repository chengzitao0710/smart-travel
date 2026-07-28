package com.smarttravel.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "旅行者信息")
@TableName("tb_traveler_info")
public class TravelerInfo {
    @TableId
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "所在城市")
    private String city;
    @Schema(description = "个人简介")
    private String introduce;
    @Schema(description = "粉丝数")
    private Integer fans;
    @Schema(description = "关注数")
    private Integer followee;
    @Schema(description = "性别 0=未知 1=男 2=女")
    private Integer gender;
    @Schema(description = "生日")
    private LocalDateTime birthday;
    @Schema(description = "积分")
    private Integer credits;
    @Schema(description = "等级")
    private Integer level;
    @Schema(description = "余额(分)")
    private Long balance;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}