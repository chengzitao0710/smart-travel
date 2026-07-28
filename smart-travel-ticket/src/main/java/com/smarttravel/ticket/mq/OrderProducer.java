package com.smarttravel.ticket.mq;

import com.smarttravel.ticket.entity.TicketOrder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    private static final int ORDER_TIMEOUT_MS = 30 * 60 * 1000;

    public void sendOrderCreate(TicketOrder order) {
        rabbitTemplate.convertAndSend(
                "order.delay.exchange",
                "order.create", order,
                msg -> {
                    msg.getMessageProperties().setHeader("x-delay", ORDER_TIMEOUT_MS);
                    return msg;
                });
        log.info("发送订单延时消息: orderId={}, orderNo={}, delay={}ms",
                order.getId(), order.getOrderNo(), ORDER_TIMEOUT_MS);
    }

    public void sendPaySuccess(Long orderId) {
        rabbitTemplate.convertAndSend("payment.exchange", "payment.create", orderId);
        log.info("发送支付成功消息: orderId={}", orderId);
    }

    public void sendOrderCancel(TicketOrder order) {
        rabbitTemplate.convertAndSend("order.cancel.exchange", "order.cancel", order);
        log.info("发送订单取消消息: orderId={}", order.getId());
    }
}