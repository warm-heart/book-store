package com.book.service.Impl;


import com.book.dao.RoleDao;
import com.book.dao.UserDao;
import com.book.entity.Role;
import com.book.entity.User;
import com.book.enums.UserEnum;
import com.book.exception.RoleException;
import com.book.exception.UserException;
import com.book.service.UserService;
import com.book.utils.KeyUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangqianlong
 * @create 2019-08-05 15:25
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private RoleDao roleDao;
    @Autowired
    private UserDao userDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;


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
        //todo 密码加密
        Integer integer = userDao.updateUser(user);
        if (integer == 1)
            return true;
        throw new UserException(UserEnum.USER_UPDATE_FAIL);
    }

    @Override
    @Transactional
    public User saveUser(User user) {
        if (StringUtils.isEmpty(user.getPassword())) {
            user.setUserPassword("");
        }
        user.setUserPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        Integer integer = userDao.saveUser(user);
        if (integer == 1)
            return userDao.findByUserId(user.getUserId());
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
    public User findByUserPhone(String userPhone) {

        User user = userDao.findByUserPhone(userPhone);
        if (user == null) {
            return null;
        }

        List<Role> roles = roleDao.findRolesByUserId(user.getUserId());
        if (roles == null || roles.isEmpty()) {
            throw new DisabledException("权限非法");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleName())));
        user.setAuthorityList(authorities);
        return user;
    }

    @Override
    public User findByUserName(String userName) {
        User user = userDao.findByUserName(userName);
        if (user == null) {
            return null;
        }
        List<Role> roles = roleDao.findRolesByUserId(user.getUserId());
        if (roles == null || roles.isEmpty()) {
            throw new RoleException("权限非法");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleName())));
        user.setAuthorityList(authorities);
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

    @Override
    @Transactional
    public User addUserByPhone(String phone) {
        User user = new User();
        String userId = KeyUtils.genUniqueKey();
        user.setUserId(userId);
        user.setUserPhone(phone);
        user.setUserName(phone.substring(0, 3) + "****" + phone.substring(7, phone.length()));


        Integer integer = userDao.saveUser(user);
        if (integer != 1) {
            throw new UserException(UserEnum.USER_SAVE_FAIL);
        }

        Role role = new Role();
        role.setRoleName("USER");
        role.setUserId(user.getUserId());
        Integer integer1 = roleDao.saveRole(role);
        if (integer1 != 1) {
            log.info("用户手机号注册保存权限失败");
            throw new UserException(UserEnum.USER_SAVE_FAIL);
        }

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        user.setAuthorityList(grantedAuthorities);

        return user;
    }
}
