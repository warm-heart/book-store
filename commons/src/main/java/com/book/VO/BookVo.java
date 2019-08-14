package com.book.VO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author wangqianlong
 * @create 2019-07-30 9:13
 */
@Getter
@Setter
@ToString
public class BookVo implements Serializable {

    private static final long serialVersionUID = 2920237858963388805L;

    private String bookId;
    private String bookName;
    private String bookDescription;
    private Integer bookStock;
    private String bookIcon;
    private BigDecimal bookPrice;
    private String categoryName;


    public BookVo(String bookId, String bookName, String bookDescription, Integer bookStock, String bookIcon, BigDecimal bookPrice, String categoryName) {
        this.bookId = bookId;
        this.bookName = bookName;
        this.bookDescription = bookDescription;
        this.bookStock = bookStock;
        this.bookIcon = bookIcon;
        this.bookPrice = bookPrice;
        this.categoryName = categoryName;
    }
}
