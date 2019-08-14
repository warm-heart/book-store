package com.book.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author wangqianlong
 * @create 2019-08-07 17:29
 */
@Data
public class People {

    private String id;
    private String name;
    private String country;
    private Integer age;
    private Date date;
}
