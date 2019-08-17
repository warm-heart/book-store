package com.book.service.Impl;

import com.book.VO.ServiceResult;
import com.book.entity.User;
import com.book.service.LoginService;
import com.book.service.UserService;
import com.book.utils.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author wangqianlong
 * @create 2019-08-16 17:05
 */
@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private UserService userService;

    @Autowired
    private SmsServiceImpl smsService;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public ServiceResult<String> login(User user, String phone, String phoneCode) {
        String Token = UUID.randomUUID().toString();
        //用户名密码登陆
        if (phone == null) {
            if (!StringUtils.isEmpty(user.getUserName())) {
                User user1 = userService.findByUserName(user.getUserName());
                if (MD5Utils.matches(user.getUserPassword(), user1.getUserPassword())) {

                    //todo redis存储token
                    redisTemplate.opsForValue().set(Token, user1, 7, TimeUnit.DAYS);
                    return ServiceResult.success(Token);
                }
                return new ServiceResult(false, "用户名或密码错误");
            }
            return new ServiceResult(false, "用户名为空");

        }

        //手机号登陆
        String smsCode = smsService.getSmsCode(phone);

        if (phoneCode == null) {
            return new ServiceResult(false, "验证码为空");
        }
        if (phoneCode.equals(phoneCode)) {
            //todo 根据电话查找用户
            // User user2 =userService.findByUserPhone()
            //todo redis存储token
            // redisTemplate.opsForValue().set(Token, user2,7,TimeUnit.DAYS);
            return ServiceResult.success(Token);
        }
        return new ServiceResult<>(false, "登陆失败，请重新登录");
    }
}

