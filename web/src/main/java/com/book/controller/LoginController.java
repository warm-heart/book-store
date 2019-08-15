package com.book.controller;

import com.book.entity.User;
import com.book.service.ISmsService;
import com.book.service.UserService;
import com.book.utils.KeyUtils;
import com.book.utils.MD5Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestParam;

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

    @PostMapping("/register")
    public String register(User user) {
        user.setUserId(KeyUtils.genUniqueKey());
        user.setUserPassword(MD5Utils.encode(user.getUserPassword()));
        log.info("接收到的数据：{}", user);
        userService.saveUser(user);
        return "index";
    }

    @GetMapping("getSmsCode")
    public void getSmsCode(String phone) {
        smsService.sendSms(phone);
    }


    @PostMapping("/login")
    public String login(User user,
                        @RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "phoneCode", required = false) String phoneCode,
                        HttpServletRequest request) {

        log.info("进入了{}", user);
        if (phone == null) {
            User user1 = userService.findByUserName(user.getUserName());
            if (MD5Utils.matches(user.getUserPassword(), user1.getUserPassword())) {
                request.getSession().setAttribute("userId", user1.getUserId());
                return "index";
            }
            return "user/login";
        }

        String smsCode = smsService.getSmsCode(phone);
        if (phoneCode == null) {
            return "user/login";
        }
        if (phoneCode.equals(phoneCode)) {
            return "index";
        }
        return "/user/login";
    }
}
