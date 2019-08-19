package com.book.controller;


import com.book.entity.User;
import com.book.service.UserService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


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


    @GetMapping("/list")
    public String list1(Model model,
                        @RequestParam(required = true, defaultValue = "1") int pageNum,
                        @RequestParam(required = true, defaultValue = "10") int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        PageInfo<User> userPageInfo = userService.getAllUser(pageNum, pageSize);
        model.addAttribute("pageInfo", userPageInfo);
        return "user/list";
    }


    @GetMapping("/toUpdateUser")
    public ModelAndView ToUpdateUser(String userId) {
        User user = userService.findByUserId(userId);
        log.info("userId是：{}", userId);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        log.info("user:{}", user);
        modelAndView.setViewName("user/update");
        return modelAndView;
    }


    @PostMapping("/updateUser")
    public String updateUser(User user) {
        log.info("接收到的数据：{}", user);
        userService.updateUser(user);
        return "user/list";
    }


    @PostMapping("/deleteUser")
    public String deleteUser(String userId) {
        log.info("接收到的数据：{}", userId);
        userService.deleteByUserId(userId);
        return "user/list";
    }


}
