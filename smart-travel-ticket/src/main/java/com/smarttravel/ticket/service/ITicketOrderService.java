package com.smarttravel.ticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smarttravel.common.dto.Result;
import com.smarttravel.ticket.dto.CreateOrderDTO;
import com.smarttravel.ticket.entity.TicketOrder;


public interface ITicketOrderService extends IService<TicketOrder> {
    Result seckill(Long ticketId);
    Result createNormalOrder(CreateOrderDTO dto);
    Result getTicketOrder(Long orderId);
    Result getTicketOrderList();
    Result payTicketOrder(Long orderId, Integer payType);
    Result cancelOrder(Long orderId);
    Result refundOrder(Long orderId);
    Result getTicketOrderPage(Integer current);
}