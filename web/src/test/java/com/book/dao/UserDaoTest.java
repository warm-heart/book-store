package com.book.dao;



import com.book.entity.User;
import com.book.utils.KeyUtils;
import com.book.utils.MD5Utils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


@Slf4j
public class UserDaoTest extends StartApplicationTests {

    @Autowired
    private UserDao userDao;




    @Test
    public void getAllUser() {
        PageHelper.startPage(1, 2);
        List<User> users = userDao.getAllUser();
        System.out.println(users);
        PageInfo<User> userPageInfo = new PageInfo<>(users);
        System.out.println(userPageInfo);
        System.out.println(userPageInfo.getList());
    }

    @Test
    public void addUser() {
        User user = new User();
        user.setUserId(KeyUtils.genUniqueKey());
        user.setUserPassword(MD5Utils.encode("1122"));
        user.setUserName("cooper");
        user.setUserAddress("安徽省滁州市");
        user.setUserEmail("1783725532@qq.com");
        user.setUserPhone("1999999099");
        Assert.assertEquals(new Integer(1), userDao.saveUser(user));

    }

    @Test
    public void updateUser() {
        User user = new User();
        user.setUserPhone("1200000000");
        user.setUserId("1");
        Integer i= userDao.UpdateUser(user);
        System.out.println(i);
    }

    @Test
    public void saveUser() {
        User user = new User();
        user.setUserPassword(MD5Utils.encode("123456"));
        user.setUserName("131472745475");
        user.setUserPhone("000000000000");
        user.setUserId(KeyUtils.genUniqueKey());
        user.setUserAddress("dfjd");
        user.setUserEmail("1291384@qq.com");
        userDao.saveUser(user);
    }


    @Test
    public void getUser() {
        log.info("输出的list {}" + userDao.findByUserId("1564622975469665420"));
    }
}