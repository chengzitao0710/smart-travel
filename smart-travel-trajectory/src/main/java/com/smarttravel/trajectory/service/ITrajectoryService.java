package com.smarttravel.trajectory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smarttravel.common.dto.Result;
import com.smarttravel.trajectory.entity.Trajectory;

public interface ITrajectoryService extends IService<Trajectory> {
    Result checkIn(Long scenicId, Long ticketId);
    Result getMyTrajectory(Integer current);
    Result getUserTrajectory(Long userId, Integer current);
    Result deleteCheckIn(Long scenicId);
}