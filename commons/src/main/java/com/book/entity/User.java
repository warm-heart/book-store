package com.book.entity;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;


/**
 * @author wangqianlong
 * @create 2019-07-29 14:29
 */

@Getter
@Setter
@ToString
//@Data
public class User implements Serializable {

    private static final long serialVersionUID = 6338526705853492368L;

    private String userId;
    private String userName;
    private String userPassword;
    private String userAddress;
    private String userEmail;
    private String userPhone;
    private Date createTime;

}
