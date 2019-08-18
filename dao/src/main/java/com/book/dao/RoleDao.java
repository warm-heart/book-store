package com.book.dao;

import com.book.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author wangqianlong
 * @create 2019-08-17 15:23
 */
@Mapper
@Repository
public interface RoleDao {

    List<Role> findRolesByUserId(String userId);

    Integer saveRole(Role role);

}
