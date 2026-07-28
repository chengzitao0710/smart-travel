package com.smarttravel.ticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "门票订单")
@TableName("tb_ticket_order")
public class TicketOrder {
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "订单ID(雪花算法)")
    private Long id;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "门票ID")
    private Long ticketId;
    @Schema(description = "景点ID")
    private Long scenicId;
    @Schema(description = "购买数量")
    private Integer count;
    @Schema(description = "支付金额(分)")
    private Long payAmount;
    @Schema(description = "支付方式 0=微信 1=支付宝")
    private Integer payType;
    @Schema(description = "状态 0=未支付 1=已支付 2=已核销 3=已退款")
    private Integer status;
    @Schema(description = "订单号")
    private String orderNo;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "支付时间")
    private LocalDateTime payTime;
    @Schema(description = "使用时间")
    private LocalDateTime useTime;
    @Schema(description = "退款时间")
    private LocalDateTime refundTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}