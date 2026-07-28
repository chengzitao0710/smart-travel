package com.smarttravel.ticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smarttravel.common.dto.Result;
import com.smarttravel.common.utils.RedisConstants;
import com.smarttravel.common.utils.SystemConstants;
import com.smarttravel.ticket.entity.SeckillTicket;
import com.smarttravel.ticket.entity.Ticket;
import com.smarttravel.ticket.mapper.SeckillTicketMapper;
import com.smarttravel.ticket.mapper.TicketMapper;
import com.smarttravel.ticket.service.ITicketService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
public class TicketServiceImpl extends ServiceImpl<TicketMapper, Ticket> implements ITicketService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SeckillTicketMapper seckillTicketMapper;

    @Override
    @Transactional
    public Result createTicket(Ticket ticket) {
        ticket.setType(SystemConstants.TICKET_STATUS_NORMAL);
        save(ticket);
        return Result.ok(ticket);
    }

    @Override
    @Transactional
    public Result createSeckillTicket(Ticket ticket) {
        ticket.setType(SystemConstants.TICKET_STATUS_SECKILL);
        save(ticket);

        saveSeckillTicketToDb(ticket);
        syncSeckillStockAfterCommit(ticket.getId(), ticket.getStock());
        return Result.ok(ticket);
    }

    @Override
    public Result getTicketListByScenic(Long scenicId) {
        List<Ticket> tickets = query()
                .eq("scenic_id", scenicId)
                .eq("type", SystemConstants.TICKET_STATUS_NORMAL)
                .list();
        return Result.ok(tickets);
    }

    @Override
    public Result getTicketById(Long id) {
        Ticket ticket = getById(id);
        if (ticket == null) {
            return Result.fail("门票不存在");
        }
        return Result.ok(ticket);
    }

    @Override
    @Transactional
    public Result updateTicket(Ticket ticket) {
        if (ticket.getId() == null) {
            return Result.fail("门票ID不能为空");
        }

        Ticket existing = getById(ticket.getId());
        if (existing == null) {
            return Result.fail("门票不存在");
        }

        updateById(ticket);

        if (Objects.equals(ticket.getType(), SystemConstants.TICKET_STATUS_SECKILL)) {
            saveSeckillTicketToDb(ticket);
            syncSeckillStockAfterCommit(ticket.getId(), ticket.getStock());
        }

        return Result.ok(getById(ticket.getId()));
    }

    @Override
    @Transactional
    public Result deleteById(Long id) {
        if (id == null) {
            return Result.fail("门票ID不能为空");
        }
        Ticket ticket = getById(id);
        if (ticket == null) {
            return Result.fail("门票不存在");
        }
        removeById(id);
        if (Objects.equals(ticket.getType(), SystemConstants.TICKET_STATUS_SECKILL)) {
            seckillTicketMapper.deleteById(id);
            deleteSeckillStockAfterCommit(id);
        }
        return Result.ok();
    }

    @Override
    public Result setTicketStatus(Long id, Integer status) {
        if (id == null || status == null) {
            return Result.fail("门票ID或状态不能为空");
        }
        Ticket ticket = getById(id);
        if (ticket == null) {
            return Result.fail("门票不存在");
        }
        ticket.setStatus(status);
        updateById(ticket);
        return Result.ok();
    }

    @Override
    public Result pageQuery(Integer page, Integer size, Long scenicId, Integer type, Integer status, String keyword) {
        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(scenicId != null, Ticket::getScenicId, scenicId)
                .eq(type != null, Ticket::getType, type)
                .eq(status != null, Ticket::getStatus, status)
                .like(StringUtils.hasText(keyword), Ticket::getTitle, keyword)
                .orderByDesc(Ticket::getCreateTime);

        Page<Ticket> result = page(new Page<>(page, size), wrapper);
        return Result.ok(result);
    }

    private void saveSeckillTicketToDb(Ticket ticket) {
        SeckillTicket seckillTicket = SeckillTicket.builder()
                .ticketId(ticket.getId())
                .stock(ticket.getStock())
                .beginTime(ticket.getValidStart())
                .endTime(ticket.getValidEnd())
                .build();

        if (seckillTicketMapper.selectById(ticket.getId()) != null) {
            seckillTicketMapper.updateById(seckillTicket);
        } else {
            seckillTicketMapper.insert(seckillTicket);
        }
    }

    private void syncSeckillStockAfterCommit(Long ticketId, Integer stock) {
        if (stock == null) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                stringRedisTemplate.opsForValue()
                        .set(RedisConstants.SECKILL_STOCK_KEY + ticketId, String.valueOf(stock));
            }
        });
    }

    private void deleteSeckillStockAfterCommit(Long ticketId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                stringRedisTemplate.delete(RedisConstants.SECKILL_STOCK_KEY + ticketId);
            }
        });
    }
}