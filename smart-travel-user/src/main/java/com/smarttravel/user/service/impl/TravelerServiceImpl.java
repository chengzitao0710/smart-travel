package com.smarttravel.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smarttravel.common.dto.LoginFormDTO;
import com.smarttravel.common.dto.Result;
import com.smarttravel.common.dto.UserDTO;
import com.smarttravel.common.utils.*;
import com.smarttravel.user.entity.Traveler;
import com.smarttravel.user.entity.TravelerInfo;
import com.smarttravel.user.mapper.TravelerMapper;
import com.smarttravel.user.service.ITravelerInfoService;
import com.smarttravel.user.service.ITravelerService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.redis.core.RedisCallback;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TravelerServiceImpl extends ServiceImpl<TravelerMapper, Traveler> implements ITravelerService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ITravelerInfoService travelerInfoService;
    @Resource
    private JwtBlackListUtils jwtBlackListUtils;
    @Resource
    private OssUtils ossUtil;

    /**
     * 发送登录验证码
     * 验证码有效期为5分钟
     * @param phone 手机号
     * @param session 会话对象
     * @return 验证码
     */
    @Override
    public Result sendCode(String phone, HttpSession session) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            log.error("手机号格式错误：{}", phone);
            return Result.fail("手机号格式错误");
        }
        String code = RandomUtil.randomNumbers(6);
        stringRedisTemplate.opsForValue().set(
                RedisConstants.LOGIN_CODE_KEY + phone,
                code,
                RedisConstants.LOGIN_CODE_TTL,
                TimeUnit.MINUTES
        );
        log.debug("发送验证码成功，验证码：{}", code);
        return Result.ok(code);
    }

    /**
     * 登录
     * 验证码有效期为5分钟
     * @param loginForm 登录表单
     * @param session 会话对象
     * @return token
     */
    @Override
    public Result loginByCode(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            log.error("手机号格式错误：{}", phone);
            return Result.fail("手机号格式错误");
        }
        String code = loginForm.getCode();
        if (code == null || code.isEmpty()) {
            return Result.fail("验证码不能为空");
        }
        String cachedCode = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);
        if (cachedCode == null || !cachedCode.equals(code)) {
            log.error("验证码错误：{}", code);
            return Result.fail("验证码错误");
        }
        stringRedisTemplate.delete(RedisConstants.LOGIN_CODE_KEY + phone);
        Traveler traveler = query().eq("phone", phone).one();
        if (traveler == null) {
            traveler = createTraveler(phone);
        }

        String token = JwtUtils.generateToken(traveler.getId(), traveler.getNickName(), traveler.getIcon(), traveler.getTokenVersion());
        initTokenVersionIfAbsent(traveler.getId(), traveler.getTokenVersion());

        kickOldSessionAndSaveToken(traveler, token);
        return Result.ok(token);
    }

    /**
     * 登录
     * 密码登录
     * @param loginForm 登录表单
     * @param session 会话对象
     * @return token
     */
    @Override
    public Result loginByPassword(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            log.error("手机号格式错误：{}", phone);
            return Result.fail("手机号格式错误");
        }
        String password = loginForm.getPassword();
        if (password == null || password.isEmpty()) {
            return Result.fail("密码不能为空");
        }
        Traveler traveler = query().eq("phone", phone).one();
        if (traveler == null) {
            return Result.fail("用户不存在");
        }
        if (!PasswordEncoder.matches(password, traveler.getPassword())) {
            log.error("密码错误");
            return Result.fail("密码错误");
        }

        String token = JwtUtils.generateToken(traveler.getId(), traveler.getNickName(), traveler.getIcon(), traveler.getTokenVersion());
        initTokenVersionIfAbsent(traveler.getId(), traveler.getTokenVersion());

        kickOldSessionAndSaveToken(traveler, token);
        return Result.ok(token);
    }

    private void kickOldSessionAndSaveToken(Traveler traveler, String token) {
        String tokenKey = RedisConstants.LOGIN_USER_KEY + traveler.getId();
        String oldToken = stringRedisTemplate.opsForValue().get(tokenKey);
        if (oldToken != null) {
            jwtBlackListUtils.addBlackList(oldToken);
        }
        stringRedisTemplate.opsForValue().set(tokenKey, token, JwtUtils.getTokenTtlMs(token), TimeUnit.MILLISECONDS);
    }

    private Traveler createTraveler(String phone) {
        String defaultPassword = phone.substring(phone.length() - 6);
        Traveler traveler = Traveler.builder()
                .phone(phone)
                .nickName(SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(6))
                .password(PasswordEncoder.encode(defaultPassword))
                .tokenVersion(0)
                .build();
        save(traveler);

        TravelerInfo travelerInfo = new TravelerInfo();
        travelerInfo.setUserId(traveler.getId());
        travelerInfoService.save(travelerInfo);

        return traveler;
    }

    /**
     * 退出登录
     * @param token token
     * @return 退出登录成功
     */
    @Override
    public Result logout(String token) {
        jwtBlackListUtils.addBlackList(token);
        Long userId = JwtUtils.getUserIdFromToken(token);
        stringRedisTemplate.delete(RedisConstants.LOGIN_USER_KEY + userId);
        UserHolder.removeUser();
        return Result.ok("退出登录成功");
    }

    /**
     * 获取当前登录信息
     * @return 当前登录用户信息
     */
    @Override
    public Result getCurrentUser() {
        UserDTO userDTO = UserHolder.getUser();
        if (userDTO == null) {
            return Result.fail("用户未登录");
        }
        return Result.ok(userDTO);
    }

    /**
     * 获取用户信息
     * @param id 用户ID
     * @return 用户信息
     */
    @Override
    public Result getUserById(Long id) {
        Traveler traveler = getById(id);
        if (traveler == null) {
            return Result.fail("用户不存在");
        }
        UserDTO userDTO = BeanUtil.copyProperties(traveler, UserDTO.class);
        return Result.ok(userDTO);
    }

    /**
     * 获取用户信息
     * @param id 用户ID
     * @return 用户信息
     */
    @Override
    public Result getUserInfo(Long id) {
        TravelerInfo info = travelerInfoService.getById(id);
        if (info == null) {
            return Result.fail("用户不存在");
        }
        return Result.ok(info);
    }

    /**
     * 签到 每日只能签到一次
     * @return 签到成功
     */
    @Override
    public Result sign() {
        UserDTO loginUser = UserHolder.getUser();
        if (loginUser == null) {
            return Result.fail("请先登录");
        }
        Long userId = loginUser.getId();
        LocalDate now = LocalDate.now();
        String key = RedisConstants.USER_SIGN_KEY + userId + ":" + now.getYear();
        int dayOfYear = now.getDayOfYear() - 1;

        Boolean isSign = stringRedisTemplate.opsForValue().getBit(key, dayOfYear);
        if (Boolean.TRUE.equals(isSign)) {
            return Result.fail("今日已签到");
        }

        stringRedisTemplate.opsForValue().setBit(key, dayOfYear, true);
        return Result.ok();
    }

    /**
     * 获取用户签到次数
     * @return 签到次数
     */
    @Override
    public Result signCount() {
        UserDTO loginUser = UserHolder.getUser();
        if (loginUser == null) {
            return Result.fail("请先登录");
        }
        Long userId = loginUser.getId();
        int year = LocalDate.now().getYear();
        String key = RedisConstants.USER_SIGN_KEY + userId + ":" + year;
        Long count = stringRedisTemplate.execute(
                (RedisCallback<Long>) connection -> connection.bitCount(key.getBytes())
        );
        return Result.ok(count != null ? count : 0);
    }

    /**
     * 获取用户本月签到状态
     * @return 本月每日签到状态列表，未登录返回 fail("请先登录")
     */
    @Override
    public Result getMonthSignRecord() {
        UserDTO loginUser = UserHolder.getUser();
        if (loginUser == null) {
            return Result.fail("请先登录");
        }
        Long userId = loginUser.getId();

        LocalDate now = LocalDate.now();
        YearMonth currentMonth = YearMonth.of(now.getYear(), now.getMonth());
        int monthTotalDays = currentMonth.lengthOfMonth();
        String signKey = RedisConstants.USER_SIGN_KEY + userId + ":" + now.getYear();

        List<Long> signStatusList = new ArrayList<>(monthTotalDays);
        for (int day = 1; day <= monthTotalDays; day++) {
            LocalDate targetDate = currentMonth.atDay(day);
            int bitOffset = targetDate.getDayOfYear() - 1;
            Boolean signed = stringRedisTemplate.opsForValue().getBit(signKey, bitOffset);
            signStatusList.add(Boolean.TRUE.equals(signed) ? 1L : 0L);
        }

        return Result.ok(signStatusList);
    }

    /**
     * 获取用户连续签到天数
     * @return 连续签到天数
     */
    @Override
    public Result signConsecutive() {
        UserDTO loginUser = UserHolder.getUser();
        if (loginUser == null) {
            return Result.fail("请先登录");
        }
        Long userId = loginUser.getId();
        LocalDate now = LocalDate.now();
        String key = RedisConstants.USER_SIGN_KEY + userId + ":" + now.getYear();
        int dayOfYear = now.getDayOfYear() - 1;

        Boolean todaySigned = stringRedisTemplate.opsForValue().getBit(key, dayOfYear);
        int startOffset = Boolean.TRUE.equals(todaySigned) ? dayOfYear : dayOfYear - 1;

        int consecutiveDays = 0;
        for (int i = startOffset; i >= 0; i--) {
            Boolean signed = stringRedisTemplate.opsForValue().getBit(key, i);
            if (Boolean.TRUE.equals(signed)) {
                consecutiveDays++;
            } else {
                break;
            }
        }

        return Result.ok(consecutiveDays);
    }

    private void initTokenVersionIfAbsent(Long userId, Integer tokenVersion) {
        String key = "jwt:version:" + userId;
        stringRedisTemplate.opsForValue().setIfAbsent(key, String.valueOf(tokenVersion != null ? tokenVersion : 0));
    }

    /**
     * 修改密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改密码成功结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result changePassword(String oldPassword, String newPassword) {
        Long userId = UserHolder.getUser().getId();
        Traveler traveler = getById(userId);
        if (traveler == null) {
            return Result.fail("用户不存在");
        }
        if (traveler.getPassword() != null) {
            if (!PasswordEncoder.matches(oldPassword, traveler.getPassword())) {
                return Result.fail("原密码错误");
            }
        }
        traveler.setPassword(PasswordEncoder.encode(newPassword));
        updateById(traveler);

        jwtBlackListUtils.updateTokenVersion(userId);
        return Result.ok();
    }

    /**
     * 更新用户信息
     * @param params 更新参数
     * @return 更新成功结果
     */
    @Override
    public Result updateTravelerInfo(Map<String, Object> params) {
        Long userId = UserHolder.getUser().getId();

        Traveler traveler = getById(userId);
        if (traveler == null) {
            return Result.fail("用户不存在");
        }
        if (params.containsKey("nickname")) {
            traveler.setNickName((String) params.get("nickname"));
        }

        updateById(traveler);

        TravelerInfo info = travelerInfoService.getById(userId);
        if (info == null) {
            return Result.fail("用户信息不存在");
        }
        if (params.containsKey("city")) {
            info.setCity((String) params.get("city"));
        }
        if (params.containsKey("introduce")) {
            info.setIntroduce((String) params.get("introduce"));
        }
        if (params.containsKey("gender")) {
            info.setGender((Integer) params.get("gender"));
        }
        if (params.containsKey("birthday")) {
            String birthdayStr = (String) params.get("birthday");
            info.setBirthday(LocalDateTime.parse(birthdayStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        travelerInfoService.updateById(info);

        UserDTO userDTO = UserDTO.builder()
                .id(userId)
                .nickname(traveler.getNickName())
                .icon(traveler.getIcon())
                .build();
        UserHolder.setUser(userDTO);

        return Result.ok();
    }

    /**
     * 上传用户头像
     * @param file 头像文件
     * @return 头像 OSS URL
     */
    @Override
    public Result uploadAvatar(MultipartFile file) {
        if (file.isEmpty()) {
            return Result.fail("文件不能为空");
        }

        Long userId = UserHolder.getUser().getId();
        Traveler traveler = getById(userId);
        if (traveler == null) {
            return Result.fail("用户不存在");
        }

        String oldIcon = traveler.getIcon();
        String url = ossUtil.upload(file);

        traveler.setIcon(url);
        updateById(traveler);

        UserDTO userDTO = UserHolder.getUser();
        userDTO.setIcon(url);
        UserHolder.setUser(userDTO);

        ossUtil.delete(oldIcon);

        return Result.ok(url);
    }

    /**
     * 删除用户头像
     * @return 删除成功
     */
    @Override
    public Result deleteAvatar() {
        Long userId = UserHolder.getUser().getId();
        Traveler traveler = getById(userId);
        if (traveler == null) {
            return Result.fail("用户不存在");
        }

        String oldIcon = traveler.getIcon();
        if (oldIcon == null || oldIcon.isBlank()) {
            return Result.ok();
        }

        traveler.setIcon(null);
        lambdaUpdate().set(Traveler::getIcon, null)
                .eq(Traveler::getId, userId)
                .update();

        UserDTO userDTO = UserHolder.getUser();
        userDTO.setIcon(null);
        UserHolder.setUser(userDTO);

        ossUtil.delete(oldIcon);

        return Result.ok();
    }
}