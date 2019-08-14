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


    @RequestMapping("/register")
    public String register(User user) {
        user.setUserId(KeyUtils.genUniqueKey());
        user.setUserPassword(MD5Utils.encode(user.getUserPassword()));
        log.info("接收到的数据：{}", user);
        userService.saveUser(user);
        return "user/login";
    }


    @RequestMapping("/login")
    public String login(User user, HttpServletRequest request) {
        User user1 = userService.findByUserName(user.getUserName());
        if (MD5Utils.matches(user.getUserPassword(), user1.getUserPassword())) {
            request.getSession().setAttribute("userId", user1.getUserId());
            return "redirect:/user/list";
        }
        return "user/login";
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
        return "redirect:/list";
    }


    @RequestMapping("/deleteUser")
    public String deleteUser(String userId) {
        log.info("接收到的数据：{}", userId);
        userService.deleteByUserId(userId);
        return "redirect:/user/list";
    }


    @GetMapping("/json")
    @ResponseBody
    public BookIndexTemplate json() {
        BookIndexTemplate bookIndexTemplate = new BookIndexTemplate();
        bookIndexTemplate.setBookId("6");
        bookIndexTemplate.setBookName("斗罗大陆");
        bookIndexTemplate.setBookDescription("古典作品");
        bookIndexTemplate.setBookStock(100);
        bookIndexTemplate.setBookPrice(79.9);
        bookIndexTemplate.setCategoryName("小说");
        bookIndexTemplate.setCreateTime(new Date());
        return bookIndexTemplate;
    }

}
