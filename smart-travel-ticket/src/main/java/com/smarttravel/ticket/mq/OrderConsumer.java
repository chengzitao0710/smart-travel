package com.smarttravel.ticket.mq;

import com.rabbitmq.client.Channel;
import com.smarttravel.common.utils.SystemConstants;
import com.smarttravel.ticket.entity.SeckillTicket;
import com.smarttravel.ticket.entity.Ticket;
import com.smarttravel.ticket.entity.TicketOrder;
import com.smarttravel.ticket.mapper.SeckillTicketMapper;
import com.smarttravel.ticket.mapper.TicketMapper;
import com.smarttravel.ticket.mapper.TicketOrderMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Component
public class OrderConsumer {

    @Resource
    private TicketOrderMapper ticketOrderMapper;

    @Resource
    private TicketMapper ticketMapper;

    @Resource
    private SeckillTicketMapper seckillTicketMapper;

    @RabbitListener(queues = "order.queue")
    @Transactional(rollbackFor = Exception.class)
    public void onOrderCreate(Message message, Channel channel, TicketOrder order) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("收到订单延时消息: orderId={}, userId={}, amount={}",
                    order.getId(), order.getUserId(), order.getPayAmount());

            TicketOrder dbOrder = ticketOrderMapper.selectById(order.getId());
            if (dbOrder == null) {
                log.warn("订单不存在, 直接确认: orderId={}", order.getId());
                channel.basicAck(deliveryTag, false);
                return;
            }

            if (Objects.equals(dbOrder.getStatus(), SystemConstants.ORDER_STATUS_UNPAID)) {
                dbOrder.setStatus(SystemConstants.ORDER_STATUS_CANCELED);
                ticketOrderMapper.updateById(dbOrder);

                Ticket ticket = ticketMapper.selectById(dbOrder.getTicketId());
                if (ticket != null) {
                    ticket.setStock(ticket.getStock() + dbOrder.getCount());
                    ticketMapper.updateById(ticket);
                }

                SeckillTicket seckillTicket = seckillTicketMapper.selectById(dbOrder.getTicketId());
                if (seckillTicket != null) {
                    seckillTicket.setStock(seckillTicket.getStock() + dbOrder.getCount());
                    seckillTicketMapper.updateById(seckillTicket);
                }

                log.info("订单超时取消, 库存已恢复: orderId={}", order.getId());
            } else {
                log.info("订单已支付或已取消, 无需处理: orderId={}, status={}",
                        order.getId(), dbOrder.getStatus());
            }

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("订单处理失败: orderId={}", order.getId(), e);
            channel.basicNack(deliveryTag, false, true);
        }
    }
}