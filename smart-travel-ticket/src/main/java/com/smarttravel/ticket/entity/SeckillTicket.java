package com.smarttravel.ticket.entity;

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
@Schema(description = "秒杀门票")
@TableName("tb_seckill_ticket")
public class SeckillTicket {
    @TableId
    @Schema(description = "门票ID")
    private Long ticketId;
    @Schema(description = "秒杀库存")
    private Integer stock;
    @Schema(description = "秒杀开始时间")
    private LocalDateTime beginTime;
    @Schema(description = "秒杀结束时间")
    private LocalDateTime endTime;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}