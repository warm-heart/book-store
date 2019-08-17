package com.book.controller;

import com.book.VO.ApiResponse;
import com.book.entity.User;
import com.book.enums.ResultEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * @author wangqianlong
 * @create 2019-08-15 11:10
 */
@Controller
public class IndexController {


    @GetMapping("/json")
    @ResponseBody
    public User test() {
        System.out.println("进入了");
        User user = new User();
        user.setUserId("1");
        user.setUserName("cooper");
        user.setUserPhone("phone");
        return user;
    }

    @GetMapping("toJson")
    public String test1() {
        return "json";
    }

    @GetMapping("json1")
    @ResponseBody
    public ApiResponse<List<User>> test2() {
        List<User> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setUserId(String.valueOf(i));
            list.add(user);
        }
        return ApiResponse.success(list, ResultEnum.SUCCESS);
    }

}
