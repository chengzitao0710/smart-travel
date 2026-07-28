package com.smarttravel.ticket.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.smarttravel.common.dto.Result;
import com.smarttravel.ticket.entity.Ticket;


public interface ITicketService extends IService<Ticket> {
    Result createTicket(Ticket ticket);
    Result createSeckillTicket(Ticket ticket);
    Result getTicketListByScenic(Long scenicId);
    Result getTicketById(Long id);
    Result updateTicket(Ticket ticket);
    Result deleteById(Long id);
    Result setTicketStatus(Long id, Integer status);
    Result pageQuery(Integer page, Integer size, Long scenicId, Integer type, Integer status, String keyword);
}