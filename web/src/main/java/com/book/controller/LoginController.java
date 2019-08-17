package com.book.controller;

import com.book.VO.ApiResponse;
import com.book.VO.ServiceResult;
import com.book.entity.User;
import com.book.enums.ResultEnum;
import com.book.service.ISmsService;
import com.book.service.LoginService;
import com.book.service.UserService;
import com.book.utils.KeyUtils;
import com.book.utils.MD5Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author wangqianlong
 * @create 2019-08-15 10:11
 */
@Slf4j
@Controller
public class LoginController {
    @Autowired
    private UserService userService;
    @Autowired
    private ISmsService smsService;

    @Autowired
    private LoginService loginService;


    @PostMapping("/register")
    public String register(User user) {
        user.setUserId(KeyUtils.genUniqueKey());
        user.setUserPassword(MD5Utils.encode(user.getUserPassword()));
        log.info("接收到的数据：{}", user);
        userService.saveUser(user);
        return "IndexController";
    }

    @GetMapping("getSmsCode")
    @ResponseBody
    public String getSmsCode(String phone) {
        smsService.sendSms(phone);
        //TODO API返回格式
        return "已发送";
    }


    @PostMapping("/login")
    @ResponseBody
    public ApiResponse login(@RequestBody User user,
                             @RequestParam(value = "phone", required = false) String phone,
                             @RequestParam(value = "phoneCode", required = false) String phoneCode,
                             HttpServletRequest request) {

        ServiceResult serviceResult = loginService.login(user, phone, phoneCode);
        if (serviceResult.isSuccess()) {
            return ApiResponse.success(serviceResult.getResult(), ResultEnum.SUCCESS);
        }
        return ApiResponse.error(serviceResult.getMessage());
    }

}
