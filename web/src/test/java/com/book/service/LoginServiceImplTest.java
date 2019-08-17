package com.book.service;

import com.book.VO.ServiceResult;
import com.book.entity.User;
import com.book.service.Impl.LoginServiceImpl;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;



/**
 * @author wangqianlong
 * @create 2019-08-16 17:30
 */

public class LoginServiceImplTest extends StartApplicationTests {

    @Autowired
    private LoginServiceImpl loginService;


    @Test
    public void test() {
        User user = new User();
        user.setUserName("cooper");
        user.setUserPassword("123");
        ServiceResult result = loginService.login(user, null, null);
        System.out.println(result);

    }
}
