package com.book.service;

import com.book.entity.User;

import com.github.pagehelper.PageInfo;



/**
 * @author wangqianlong
 * @create 2019-08-05 15:25
 */

public interface UserService {


    public PageInfo<User> getAllUser(Integer pageNum, Integer pageSize);


    public Integer updateUser(User user);

    public Integer saveUser(User user);

    public User findByUserId(String userId);

    public User findByUserName(String userName);

    public Integer deleteByUserId(String userId);
}
