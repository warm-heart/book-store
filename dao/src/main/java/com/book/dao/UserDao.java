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
     * 修改用户信息
     * @param user
     * @return
     */
    public Integer updateUser(User user);

    /**
     * 保存用户信息
     * @param user
     * @return
     */
    public Integer saveUser(User user);

    /**
     * 根据用户Id查找
     * @param userId
     * @return
     */
    public User findByUserId(String userId);

    /**
     * 根据用户姓名查找
     * @param userName
     * @return
     */

    public User findByUserName(String userName);


    /**
     * 根据用户电话查找
     * @param userPhone
     * @return
     */
    public User findByUserPhone(String userPhone);

    /**
     * 根据Id删除
     * @param userId
     * @return
     */
    public Integer deleteByUserId(String userId);
}
