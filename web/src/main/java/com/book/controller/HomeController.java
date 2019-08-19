package com.book.controller;

import com.book.VO.ApiResponse;
import com.book.entity.Role;
import com.book.enums.ResultEnum;
import com.book.service.ISmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collections;

/**
 * @author wangqianlong
 * @create 2019-08-15 10:11
 */
@Slf4j
@Controller
public class HomeController {

    @Autowired
    private ISmsService smsService;


    @GetMapping(value = "sms/code")
    @ResponseBody
    public ApiResponse getSmsCode(String phone) {
        smsService.sendSms(phone);
        return ApiResponse.success("", ResultEnum.SUCCESS);
    }


    @GetMapping("/logout/page")
    public String logoutPage() {
        return "logout";
    }

    @GetMapping("/user/login")
    public String userLogin() {
        return "user/login";
    }


    @GetMapping("/getUserMsg")
    @ResponseBody
    public SecurityContext getSecurityDetail(HttpServletRequest request) {
        SecurityContext context = SecurityContextHolder.getContext();
//        SecurityContext context = (SecurityContext) request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
//        User user = (User) context.getAuthentication().getPrincipal();
//        System.out.println(request.getSession().getId());
//        System.out.println(user);
        return context;
    }

}
