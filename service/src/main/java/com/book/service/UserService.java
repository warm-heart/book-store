package com.book.service;

import com.book.entity.User;

import com.github.pagehelper.PageInfo;


/**
 * @author wangqianlong
 * @create 2019-08-05 15:25
 */

public interface UserService {


    /**
     *  获取所有用户
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageInfo<User> getAllUser(Integer pageNum, Integer pageSize);


    /**
     * 修改用户信息
     * @param user
     * @return
     */
    public boolean updateUser(User user);

    /**
     * 保存用户
     * @param user
     * @return
     */
    public User saveUser(User user);

    /**
     * 根据用户Id查找
     * @param userId
     * @return
     */
    public User findByUserId(String userId);

    /**
     * 通过电话查找
     * @param userPhone
     * @return
     */
    public User findByUserPhone(String userPhone);

    /**
     * 根据用户名查找
     * @param userName
     * @return
     */
    public User findByUserName(String userName);

    /**
     * 根据用户Id删除用户
     * @param userId
     * @return
     */
    public boolean deleteByUserId(String userId);

    /**
     * 根据用户电话注册用户
     * @param phone
     * @return
     */
    public User addUserByPhone(String phone);
}
