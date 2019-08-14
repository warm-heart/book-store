package com.book.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author wangqianlong
 * @create 2019-07-30 9:10
 */
@Getter
@Setter
@ToString
public class Role implements Serializable {


    private static final long serialVersionUID = 6681271145545945498L;

    private Integer roleId;
    private String roleDescription;
    private String userId;


}
