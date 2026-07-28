package com.smarttravel.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smarttravel.user.entity.TravelerInfo;
import com.smarttravel.user.mapper.TravelerInfoMapper;
import com.smarttravel.user.service.ITravelerInfoService;
import org.springframework.stereotype.Service;

@Service
public class TravelerInfoServiceImpl extends ServiceImpl<TravelerInfoMapper, TravelerInfo> implements ITravelerInfoService {
}