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
    public Integer updateUser(User user) {
        return userDao.UpdateUser(user);
    }

    @Override
    public Integer saveUser(User user) {
        return userDao.saveUser(user);
    }

    @Override
    public User findByUserId(String userId) {
        return userDao.findByUserId(userId);
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
    public Integer deleteByUserId(String userId) {
        return userDao.deleteByUserId(userId);
    }
}
