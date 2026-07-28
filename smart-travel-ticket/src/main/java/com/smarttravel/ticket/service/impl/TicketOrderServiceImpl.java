package com.smarttravel.ticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smarttravel.common.dto.Result;
import com.smarttravel.common.utils.RedisConstants;
import com.smarttravel.common.utils.RedisIdWorker;
import com.smarttravel.common.utils.SystemConstants;
import com.smarttravel.common.utils.UserHolder;
import com.smarttravel.ticket.dto.CreateOrderDTO;
import com.smarttravel.ticket.entity.SeckillTicket;
import com.smarttravel.ticket.entity.Ticket;
import com.smarttravel.ticket.entity.TicketOrder;
import com.smarttravel.ticket.mapper.SeckillTicketMapper;
import com.smarttravel.ticket.mapper.TicketMapper;
import com.smarttravel.ticket.mapper.TicketOrderMapper;
import com.smarttravel.ticket.mq.OrderProducer;
import com.smarttravel.ticket.service.ITicketOrderService;
import com.smarttravel.user.entity.TravelerInfo;
import com.smarttravel.user.mapper.TravelerInfoMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;

@Service
@Slf4j
public class TicketOrderServiceImpl extends ServiceImpl<TicketOrderMapper, TicketOrder> implements ITicketOrderService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private TicketMapper ticketMapper;

    @Resource
    private SeckillTicketMapper seckillTicketMapper;

    @Resource
    private TicketOrderMapper ticketOrderMapper;

    @Resource
    private OrderProducer orderProducer;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private TravelerInfoMapper travelerInfoMapper;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);

        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }

    @Override
    public Result seckill(Long ticketId) {
        Long userId = UserHolder.getUser().getId();

        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                ticketId.toString(),
                userId.toString()
        );

        if (Objects.equals(result, SystemConstants.SECKILL_STOCK_ERROR)) {
            return Result.fail("库存不足");
        }

        if (Objects.equals(result, SystemConstants.SECKILL_ORDER_ERROR)) {
            return Result.fail("您已抢购过该门票");
        }

        RLock lock = redissonClient.getLock(RedisConstants.LOCK_SECKILL_KET + ticketId);
        try {
            if (lock.tryLock(RedisConstants.LOCK_SECKILL_TTL, TimeUnit.SECONDS)) {
                return createOrder(ticketId, userId);
            }
        } catch (InterruptedException e) {
            log.error("抢购门票失败", e);
            Thread.currentThread().interrupt();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return Result.ok();
    }

    public Result createOrder(Long ticketId, Long userId) {
        return transactionTemplate.execute(status -> {
            Ticket ticket = ticketMapper.selectById(ticketId);
            if (ticket == null) {
                return Result.fail("门票不存在");
            }

            SeckillTicket seckillTicket = seckillTicketMapper.selectById(ticketId);
            if (seckillTicket == null || seckillTicket.getStock() <= 0) {
                return Result.fail("库存不足");
            }

            seckillTicket.setStock(seckillTicket.getStock() - 1);
            ticket.setStock(ticket.getStock() - 1);
            ticketMapper.updateById(ticket);
            seckillTicketMapper.updateById(seckillTicket);

            TicketOrder order = TicketOrder.builder()
                    .userId(userId)
                    .ticketId(ticketId)
                    .scenicId(ticket.getScenicId())
                    .count(1)
                    .payAmount(ticket.getPayValue())
                    .status(SystemConstants.ORDER_STATUS_UNPAID)
                    .orderNo(String.valueOf(redisIdWorker.nextId("order")))
                    .build();

            ticketOrderMapper.insert(order);

            orderProducer.sendOrderCreate(order);

            return Result.ok(order);
        });
    }

    @Override
    @Transactional
    public Result createNormalOrder(CreateOrderDTO dto) {
        if (dto == null || dto.getTicketId() == null || dto.getCount() == null || dto.getCount() <= 0) {
            return Result.fail("参数不合法");
        }

        Long userId = UserHolder.getUser().getId();

        Ticket ticket = ticketMapper.selectById(dto.getTicketId());
        if (ticket == null) {
            return Result.fail("门票不存在");
        }

        if (ticket.getStock() == null || ticket.getStock() < dto.getCount()) {
            return Result.fail("库存不足");
        }

        ticket.setStock(ticket.getStock() - dto.getCount());
        ticketMapper.updateById(ticket);

        TicketOrder order = TicketOrder.builder()
                .userId(userId)
                .ticketId(dto.getTicketId())
                .scenicId(ticket.getScenicId())
                .count(dto.getCount())
                .payAmount(ticket.getPayValue() * dto.getCount())
                .status(SystemConstants.ORDER_STATUS_UNPAID)
                .orderNo(String.valueOf(redisIdWorker.nextId("order")))
                .build();

        ticketOrderMapper.insert(order);

        orderProducer.sendOrderCreate(order);

        return Result.ok(order);
    }

    @Override
    public Result getTicketOrder(Long orderId) {
        Long userId = UserHolder.getUser().getId();
        TicketOrder order = ticketOrderMapper.selectById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        if (!Objects.equals(order.getUserId(), userId)) {
            return Result.fail("您没有权限查看该订单");
        }
        return Result.ok(order);
    }

    @Override
    public Result getTicketOrderList() {
        Long userId = UserHolder.getUser().getId();

        List<TicketOrder> orders = ticketOrderMapper.selectList(new LambdaQueryWrapper<TicketOrder>()
                .eq(TicketOrder::getUserId, userId));

        return Result.ok(orders);
    }

    @Override
    @Transactional
    public Result payTicketOrder(Long orderId, Integer payType) {
        Long userId = UserHolder.getUser().getId();

        TicketOrder order = ticketOrderMapper.selectById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }

        if (!Objects.equals(order.getUserId(), userId)) {
            return Result.fail("您没有权限支付该订单");
        }
        if (!Objects.equals(order.getStatus(), SystemConstants.ORDER_STATUS_UNPAID)) {
            return Result.fail("订单状态错误");
        }

        if (Objects.equals(payType, SystemConstants.PAY_TYPE_BALANCE)) {
            TravelerInfo travelerInfo = travelerInfoMapper.selectById(userId);
            if (travelerInfo == null || travelerInfo.getBalance() == null
                    || travelerInfo.getBalance() < order.getPayAmount()) {
                return Result.fail("余额不足");
            }

            order.setPayType(payType);
            ticketOrderMapper.updateById(order);

            orderProducer.sendPaySuccess(order.getId());
            log.info("余额支付发起，发送支付成功消息：orderId={}, amount={}", order.getId(), order.getPayAmount());

            return Result.ok(order);
        }

        order.setPayType(payType);
        ticketOrderMapper.updateById(order);

        HashMap<String, Object> payParams = new HashMap<>();
        payParams.put("orderId", order.getId());
        payParams.put("orderNo", order.getOrderNo());
        payParams.put("payAmount", order.getPayAmount());

        return Result.ok(payParams);
    }

    @Override
    @Transactional
    public Result cancelOrder(Long orderId) {
        Long userId = UserHolder.getUser().getId();

        TicketOrder order = ticketOrderMapper.selectById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        if (!Objects.equals(order.getUserId(), userId)) {
            return Result.fail("您没有权限操作该订单");
        }
        if (!Objects.equals(order.getStatus(), SystemConstants.ORDER_STATUS_UNPAID)) {
            return Result.fail("仅未支付订单可取消");
        }

        releaseStock(order);

        order.setStatus(SystemConstants.ORDER_STATUS_CANCELED);
        ticketOrderMapper.updateById(order);

        orderProducer.sendOrderCancel(order);
        log.info("订单已取消：orderId={}", order.getId());
        return Result.ok();
    }

    @Override
    @Transactional
    public Result refundOrder(Long orderId) {
        Long userId = UserHolder.getUser().getId();

        TicketOrder order = ticketOrderMapper.selectById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        if (!Objects.equals(order.getUserId(), userId)) {
            return Result.fail("您没有权限操作该订单");
        }
        if (!Objects.equals(order.getStatus(), SystemConstants.ORDER_STATUS_PAID)) {
            return Result.fail("仅已支付订单可退款");
        }

        if (Objects.equals(order.getPayType(), SystemConstants.PAY_TYPE_BALANCE)) {
            TravelerInfo travelerInfo = travelerInfoMapper.selectById(userId);
            if (travelerInfo != null) {
                travelerInfo.setBalance(travelerInfo.getBalance() + order.getPayAmount());
                travelerInfoMapper.updateById(travelerInfo);
            }
        }

        releaseStock(order);

        order.setStatus(SystemConstants.ORDER_STATUS_REFUNDED);
        order.setRefundTime(LocalDateTime.now());
        ticketOrderMapper.updateById(order);

        log.info("订单已退款：orderId={}, amount={}", order.getId(), order.getPayAmount());
        return Result.ok();
    }

    @Override
    public Result getTicketOrderPage(Integer current) {
        Long userId = UserHolder.getUser().getId();

        Page<TicketOrder> page = new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE);
        page = ticketOrderMapper.selectPage(page,
                new LambdaQueryWrapper<TicketOrder>()
                        .eq(TicketOrder::getUserId, userId)
                        .orderByDesc(TicketOrder::getCreateTime));

        return Result.ok(page.getRecords(), page.getTotal());
    }

    private void releaseStock(TicketOrder order) {
        Ticket ticket = ticketMapper.selectById(order.getTicketId());
        if (ticket != null) {
            ticket.setStock(ticket.getStock() + order.getCount());
            ticketMapper.updateById(ticket);
        }

        SeckillTicket seckillTicket = seckillTicketMapper.selectById(order.getTicketId());
        if (seckillTicket != null) {
            seckillTicket.setStock(seckillTicket.getStock() + order.getCount());
            seckillTicketMapper.updateById(seckillTicket);
        }
    }
}