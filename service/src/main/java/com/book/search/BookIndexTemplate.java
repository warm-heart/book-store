package com.book.search;

import lombok.Data;

import java.util.Date;

/**
 * @author wangqianlong
 * @create 2019-08-07 20:53
 */
@Data
public class BookIndexTemplate {
    private String bookId;
    private String bookName;
    private String bookDescription;
    private Integer bookStock;
    private Double bookPrice;
    private String categoryName;
    private Date createTime;
}
