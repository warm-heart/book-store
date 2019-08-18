package com.book.exception;

import com.book.enums.BookEnum;
import lombok.Data;

/**
 * @author wangqianlong
 * @create 2019-05-02 10:03
 */
public class BookException extends RuntimeException {
    private Integer code;

    public BookException(BookEnum bookEnum) {
        super(bookEnum.getMessage());
        this.code = bookEnum.getCode();
    }

    public BookException(String message, Throwable cause) {
        super(message, cause);
    }

    public BookException(String message) {
        super(message);
    }

    public BookException(Integer code, String message) {
        super(message);
        this.code = code;
    }
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
