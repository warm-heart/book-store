package com.book.service.Impl;


import com.book.dao.UserDao;
import com.book.entity.User;
import com.book.enums.UserEnum;
import com.book.exception.UserException;
import com.book.service.UserService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author wangqianlong
 * @create 2019-08-05 15:25
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public PageInfo<User> getAllUser(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> users = userDao.getAllUser();
        PageInfo<User> userPageInfo = new PageInfo<>(users);
        return userPageInfo;
    }

    @Override
    @Transactional
    public boolean updateUser(User user) {

        Integer integer = userDao.UpdateUser(user);
        if (integer == 1)
            return true;
        throw new UserException(UserEnum.USER_UPDATE_FAIL);
    }

    @Override
    @Transactional
    public boolean saveUser(User user) {
        Integer integer = userDao.saveUser(user);
        if (integer == 1)
            return true;
        throw new UserException(UserEnum.USER_SAVE_FAIL);
    }

    @Override
    public User findByUserId(String userId) {

        User user = userDao.findByUserId(userId);
        if (user == null) {
            throw new UserException(UserEnum.USER_NOT_EXIST);
        }
        return user;

    }

    @Override
    public User findByUserName(String userName) {
        User user = userDao.findByUserName(userName);
        if (user == null) {
            throw new UserException(UserEnum.USER_NOT_EXIST);
        }
        return user;
    }

    @Override
    @Transactional
    public boolean deleteByUserId(String userId) {
        Integer integer = userDao.deleteByUserId(userId);
        if (integer == 1)
            return true;
        throw new UserException(UserEnum.USER_DELETE_FAIL);

    }
}
