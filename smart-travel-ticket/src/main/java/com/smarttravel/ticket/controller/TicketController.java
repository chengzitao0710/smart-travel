package com.smarttravel.ticket.controller;

import com.smarttravel.common.dto.Result;
import com.smarttravel.ticket.entity.Ticket;
import com.smarttravel.ticket.service.ITicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "门票管理")
@RestController
@RequestMapping("/ticket")
public class TicketController {

    @Resource
    private ITicketService ticketService;

    @Operation(summary = "创建普通门票")
    @PostMapping
    public Result createTicket(@RequestBody Ticket ticket) {
        log.info("创建普通门票: {}", ticket);
        return ticketService.createTicket(ticket);
    }

    @Operation(summary = "创建秒杀门票")
    @PostMapping("/seckill")
    public Result createSeckillTicket(@RequestBody Ticket ticket) {
        log.info("创建秒杀门票: {}", ticket);
        return ticketService.createSeckillTicket(ticket);
    }

    @Operation(summary = "获取景点下的门票列表")
    @GetMapping("/list/{scenicId}")
    public Result getTicketListByScenic(
            @Parameter(description = "景点ID") @PathVariable("scenicId") Long scenicId) {
        log.info("获取景点下的门票列表: {}", scenicId);
        return ticketService.getTicketListByScenic(scenicId);
    }

    @Operation(summary = "根据ID获取门票详情")
    @GetMapping("/{id}")
    public Result getTicketById(
            @Parameter(description = "门票ID") @PathVariable("id") Long id) {
        log.info("根据ID获取门票详情: {}", id);
        return ticketService.getTicketById(id);
    }

    @Operation(summary = "更新门票")
    @PutMapping
    public Result updateTicket(@RequestBody Ticket ticket) {
        log.info("更新门票: {}", ticket);
        return ticketService.updateTicket(ticket);
    }

    @Operation(summary = "删除门票")
    @DeleteMapping("/{id}")
    public Result deleteTicket(
            @Parameter(description = "门票ID") @PathVariable("id") Long id) {
        log.info("删除门票: {}", id);
        return ticketService.deleteById(id);
    }

    @Operation(summary = "设置门票状态")
    @PutMapping("/{id}/status")
    public Result setTicketStatus(
            @Parameter(description = "门票ID") @PathVariable("id") Long id,
            @Parameter(description = "状态 0=下架 1=上架") @RequestParam("status") Integer status) {
        log.info("设置门票状态: id={}, status={}", id, status);
        return ticketService.setTicketStatus(id, status);
    }

    @Operation(summary = "分页查询门票")
    @GetMapping("/page")
    public Result pageQuery(
            @Parameter(description = "当前页") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(value = "size", defaultValue = "10") Integer size,
            @Parameter(description = "景点ID(可选)") @RequestParam(value = "scenicId", required = false) Long scenicId,
            @Parameter(description = "门票类型(可选)") @RequestParam(value = "type", required = false) Integer type,
            @Parameter(description = "状态(可选)") @RequestParam(value = "status", required = false) Integer status,
            @Parameter(description = "关键词(可选)") @RequestParam(value = "keyword", required = false) String keyword) {
        log.info("分页查询门票: page={}, size={}, scenicId={}, type={}, status={}, keyword={}",
                page, size, scenicId, type, status, keyword);
        return ticketService.pageQuery(page, size, scenicId, type, status, keyword);
    }
}