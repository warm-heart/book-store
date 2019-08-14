package com.book.dao;


import com.book.entity.User;
import org.apache.ibatis.annotations.Mapper;

import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * @author wangqianlong
 * @create 2019-07-29 15:42
 */
@Mapper
@Repository
public interface UserDao {


    /**
     * 获取所有用户
     * @return
     */
    public List<User> getAllUser();


    /**
     * 修改用户
     * @param user
     * @return
     */
    public Integer UpdateUser(User user);

    /**
     * 保存用户
     * @param user
     * @return
     */
    public Integer saveUser(User user);

    /**
     * 根据用户id查找
     * @param userId
     * @return
     */
    public User findByUserId(String userId);

    /**
     * 根据用户姓名查找
     * @param UserName
     * @return
     */
    public User findByUserName(String UserName);

    /**
     * 根据id删除
     * @param userId
     * @return
     */
    public Integer deleteByUserId(String userId);
}
