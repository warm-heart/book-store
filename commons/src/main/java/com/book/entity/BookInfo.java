package com.book.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author wangqianlong
 * @create 2019-07-30 9:13
 */
@Getter
@Setter
@ToString
public class BookInfo implements Serializable {

    private static final long serialVersionUID = 2920237858963388805L;

    private String bookId;
    private String bookName;
    private String bookDescription;
    private Integer bookStock;
    private String bookIcon;
    private BigDecimal bookPrice;
    private Integer categoryType;
    private Date createTime;
    private Date updateTime;


}
