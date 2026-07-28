package com.smarttravel.ticket.controller;

import com.smarttravel.common.dto.Result;
import com.smarttravel.ticket.dto.CreateOrderDTO;
import com.smarttravel.ticket.service.ITicketOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "门票订单管理")
@RestController
@RequestMapping("/ticket-order")
public class TicketOrderController {

    @Resource
    private ITicketOrderService ticketOrderService;

    @Operation(summary = "秒杀门票")
    @PostMapping("/seckill/{id}")
    public Result seckill(
            @Parameter(description = "门票ID") @PathVariable("id") Long ticketId) {
        log.info("秒杀门票: {}", ticketId);
        return ticketOrderService.seckill(ticketId);
    }

    @Operation(summary = "普通购票")
    @PostMapping
    public Result createOrder(@RequestBody CreateOrderDTO dto) {
        log.info("创建订单: ticketId={}, count={}", dto.getTicketId(), dto.getCount());
        return ticketOrderService.createNormalOrder(dto);
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/{id}")
    public Result getTicketOrder(
            @Parameter(description = "订单ID") @PathVariable("id") Long orderId) {
        log.info("获取订单详情: {}", orderId);
        return ticketOrderService.getTicketOrder(orderId);
    }

    @Operation(summary = "获取用户订单列表")
    @GetMapping("/user")
    public Result getTicketOrderList() {
        log.info("获取用户订单列表");
        return ticketOrderService.getTicketOrderList();
    }

    @Operation(summary = "支付订单")
    @PostMapping("/pay/{id}")
    @ResponseBody
    public Result payTicketOrder(
            @Parameter(description = "订单ID") @PathVariable("id") Long orderId,
            @Parameter(description = "支付方式 0=微信 1=支付宝") @RequestParam Integer payType) {
        log.info("支付订单: orderId={}, payType={}", orderId, payType);
        return ticketOrderService.payTicketOrder(orderId, payType);
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    public Result cancelOrder(
            @Parameter(description = "订单ID") @PathVariable("id") Long orderId) {
        log.info("取消订单: {}", orderId);
        return ticketOrderService.cancelOrder(orderId);
    }

    @Operation(summary = "退款")
    @PutMapping("/{id}/refund")
    public Result refundOrder(
            @Parameter(description = "订单ID") @PathVariable("id") Long orderId) {
        log.info("退款: {}", orderId);
        return ticketOrderService.refundOrder(orderId);
    }

    @Operation(summary = "分页查询订单")
    @GetMapping("/page")
    public Result getTicketOrderPage(
            @Parameter(description = "当前页") @RequestParam(value = "current", defaultValue = "1") Integer current) {
        log.info("分页查询订单: current={}", current);
        return ticketOrderService.getTicketOrderPage(current);
    }
}