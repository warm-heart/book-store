package com.book.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author wangqianlong
 * @create 2019-07-30 9:10
 */
@Data
public class Role implements Serializable {


    private static final long serialVersionUID = 6681271145545945498L;

    private Integer roleId;
    private String roleName;
    private String userId;

    public Role() {
    }

    public Role(Integer roleId, String roleName, String userId) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.userId = userId;
    }
}
