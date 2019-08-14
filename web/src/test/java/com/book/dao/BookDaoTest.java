package com.book.dao;


import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class BookDaoTest extends StartApplicationTests {
    @Autowired
    UserDao userDao;

    @Test
    public void findAllBookInfo() {
        System.out.println(userDao.getAllUser());
    }
}