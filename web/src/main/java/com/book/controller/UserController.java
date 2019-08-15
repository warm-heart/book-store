package com.book.controller;


import com.book.entity.User;
import com.book.search.BookIndexTemplate;
import com.book.service.UserService;
import com.book.utils.KeyUtils;
import com.book.utils.MD5Utils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;


/**
 * @author wangqianlong
 * @create 2019-07-29 12:02
 */
@Slf4j
@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;


    @RequestMapping("/list")
    public String list1(Model model,
                        @RequestParam(required = true, defaultValue = "1") int pageNum,
                        @RequestParam(required = true, defaultValue = "10") int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        PageInfo<User> userPageInfo = userService.getAllUser(pageNum, pageSize);
        model.addAttribute("pageInfo", userPageInfo);
        return "user/list";
    }


    @RequestMapping("/toUpdateUser")
    public ModelAndView ToUpdateUser(String userId) {
        User user = userService.findByUserId(userId);
        log.info("userId是：{}", userId);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        log.info("user:{}", user);
        modelAndView.setViewName("user/update");
        return modelAndView;
    }


    @RequestMapping("/updateUser")
    public String updateUser(User user) {
        log.info("接收到的数据：{}", user);
        userService.updateUser(user);
        return "index";
    }


    @RequestMapping("/deleteUser")
    public String deleteUser(String userId) {
        log.info("接收到的数据：{}", userId);
        userService.deleteByUserId(userId);
        return "index";
    }


}
