package com.smarttravel.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "创建订单请求")
public class CreateOrderDTO {
    @Schema(description = "门票ID")
    private Long ticketId;
    @Schema(description = "购买数量")
    private Integer count;
}