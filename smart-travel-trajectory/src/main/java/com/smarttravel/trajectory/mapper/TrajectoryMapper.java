package com.smarttravel.trajectory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarttravel.trajectory.entity.Trajectory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TrajectoryMapper extends BaseMapper<Trajectory> {
}