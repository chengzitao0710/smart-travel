package com.smarttravel.trajectory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smarttravel.common.dto.Result;
import com.smarttravel.common.utils.SystemConstants;
import com.smarttravel.common.utils.UserHolder;
import com.smarttravel.ticket.entity.TicketOrder;
import com.smarttravel.ticket.mapper.TicketOrderMapper;
import com.smarttravel.trajectory.entity.Trajectory;
import com.smarttravel.trajectory.mapper.TrajectoryMapper;
import com.smarttravel.trajectory.service.ITrajectoryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrajectoryServiceImpl extends ServiceImpl<TrajectoryMapper, Trajectory> implements ITrajectoryService {

    @Resource
    private TicketOrderMapper ticketOrderMapper;

    @Override
    @Transactional
    public Result checkIn(Long scenicId, Long ticketId) {
        Long userId = UserHolder.getUser().getId();

        Trajectory exist = query()
                .eq("user_id", userId)
                .eq("scenic_id", scenicId)
                .one();
        if (exist != null) {
            return Result.fail("该景点已打卡，无需重复打卡");
        }

        TicketOrder ticket = ticketOrderMapper.selectOne(new QueryWrapper<TicketOrder>()
                .eq("user_id", userId)
                .eq("ticket_id", ticketId)
                .eq("scenic_id", scenicId)
                .eq("status", SystemConstants.ORDER_STATUS_PAID)
                .last("LIMIT 1"));

        if (ticket == null) {
            return Result.fail("该景点未购买，无需打卡");
        }
        ticket.setStatus(SystemConstants.ORDER_STATUS_VERIFIED);
        ticketOrderMapper.updateById(ticket);

        Trajectory trajectory = Trajectory.builder()
                .userId(userId)
                .scenicId(scenicId)
                .build();
        save(trajectory);
        return Result.ok("打卡成功");
    }

    @Override
    public Result getMyTrajectory(Integer current) {
        Long userId = UserHolder.getUser().getId();

        Page<Trajectory> page = query()
                .eq("user_id", userId)
                .orderByDesc("create_time")
                .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));

        return Result.ok(page.getRecords(), page.getTotal());
    }

    @Override
    public Result getUserTrajectory(Long userId, Integer current) {
        Page<Trajectory> page = query()
                .eq("user_id", userId)
                .orderByDesc("create_time")
                .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));

        return Result.ok(page.getRecords(), page.getTotal());
    }

    @Override
    @Transactional
    public Result deleteCheckIn(Long scenicId) {
        Long userId = UserHolder.getUser().getId();

        remove(new QueryWrapper<Trajectory>()
                .eq("user_id", userId)
                .eq("scenic_id", scenicId));

        return Result.ok("删除成功");
    }
}