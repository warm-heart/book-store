package com.book.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author wangqianlong
 * @create 2019-07-30 9:18
 */
@Getter
@Setter
@ToString
public class BookCategory implements Serializable {

    private static final long serialVersionUID = -4517769331113370172L;

    private Integer categoryId;

    private String categoryName;

    private Integer categoryType;


}
