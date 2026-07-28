package com.smarttravel.ticket.mq;

import com.rabbitmq.client.Channel;
import com.smarttravel.common.utils.SystemConstants;
import com.smarttravel.scenic.entity.Scenic;
import com.smarttravel.scenic.mapper.ScenicMapper;
import com.smarttravel.scenic.service.ScenicEsService;
import com.smarttravel.ticket.entity.TicketOrder;
import com.smarttravel.ticket.mapper.TicketOrderMapper;
import com.smarttravel.user.entity.TravelerInfo;
import com.smarttravel.user.mapper.TravelerInfoMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;


@Slf4j
@Component
public class PaymentConsumer {

    @Resource
    private TicketOrderMapper ticketOrderMapper;

    @Resource
    private TravelerInfoMapper travelerInfoMapper;

    @Resource
    private ScenicMapper scenicMapper;

    @Resource
    private ScenicEsService scenicEsService;

    @RabbitListener(queues = "payment.queue")
    @Transactional(rollbackFor = Exception.class)
    public void onPaySuccess(Message message, Channel channel, Long orderId) throws IOException {

        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            log.info("收到支付成功消息：orderId={}", orderId);
            TicketOrder order = ticketOrderMapper.selectById(orderId);

            if (order == null) {
                log.warn("订单不存在，orderId={}", orderId);
                channel.basicAck(deliveryTag, false);
                return;
            }
            if (Objects.equals(order.getStatus(), SystemConstants.ORDER_STATUS_UNPAID)) {
                if (Objects.equals(order.getPayType(), SystemConstants.PAY_TYPE_BALANCE)) {
                    TravelerInfo travelerInfo = travelerInfoMapper.selectById(order.getUserId());
                    if (travelerInfo == null || travelerInfo.getBalance() == null
                            || travelerInfo.getBalance() < order.getPayAmount()) {
                        log.error("余额不足，无法完成支付：orderId={}, userId={}", orderId, order.getUserId());
                        channel.basicAck(deliveryTag, false);
                        return;
                    }
                    travelerInfo.setBalance(travelerInfo.getBalance() - order.getPayAmount());
                    travelerInfoMapper.updateById(travelerInfo);
                    log.info("余额扣减成功：userId={}, amount={}, remain={}",
                            order.getUserId(), order.getPayAmount(), travelerInfo.getBalance());
                }

                order.setStatus(SystemConstants.ORDER_STATUS_PAID);
                order.setPayTime(LocalDateTime.now());
                ticketOrderMapper.updateById(order);
                log.info("订单支付成功: orderId={}, orderNo={}, amount={}",
                        order.getId(), order.getOrderNo(), order.getPayAmount());

                try {
                    scenicMapper.incrementSold(order.getScenicId());
                    log.info("景点销量+1: scenicId={}", order.getScenicId());
                    Scenic scenic = scenicMapper.selectById(order.getScenicId());
                    if (scenic != null) {
                        scenicEsService.updateScenic(scenic);
                    }
                } catch (Exception e) {
                    log.warn("景点销量更新失败: scenicId={}", order.getScenicId(), e);
                }
            } else {
                log.info("订单已支付或非待支付状态, 跳过处理: orderId={}, status={}",
                        orderId, order.getStatus());
            }

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("支付回调处理失败: orderId={}", orderId, e);
            channel.basicNack(deliveryTag, false, true);
        }
    }
}