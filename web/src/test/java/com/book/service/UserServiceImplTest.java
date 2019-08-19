package com.book.service;


import com.book.entity.User;
import com.book.utils.KeyUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author wangqianlong
 * @create 2019-08-16 17:30
 */

public class UserServiceImplTest extends StartApplicationTests {

    @Autowired
    private UserService userService;


    @Test
    public void test() {
        User user = new User();
        user.setUserId(KeyUtils.genUniqueKey());
        user.setUserName("cooper");
        user.setUserPassword("1122");
        User u = userService.saveUser(user);
        System.out.println(u);

    }


}
