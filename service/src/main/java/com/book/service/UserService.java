package com.book.service;

import com.book.entity.User;

import com.github.pagehelper.PageInfo;



/**
 * @author wangqianlong
 * @create 2019-08-05 15:25
 */

public interface UserService {


    public PageInfo<User> getAllUser(Integer pageNum, Integer pageSize);


    public boolean updateUser(User user);

    public boolean saveUser(User user);

    public User findByUserId(String userId);

    public User findByUserName(String userName);

    public boolean deleteByUserId(String userId);
}
