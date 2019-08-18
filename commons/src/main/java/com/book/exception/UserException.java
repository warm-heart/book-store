package com.book.exception;


import com.book.enums.UserEnum;
import lombok.Data;

/**
 * @author wangqianlong
 * @create 2019-05-02 10:03
 */

public class UserException extends RuntimeException {
    private Integer code;

    public UserException(UserEnum userEnum) {
        super(userEnum.getMessage());
        this.code = userEnum.getCode();
    }


    public UserException(String message, Throwable cause ) {
        super(message, cause);
    }

    public UserException(String message) {
        super(message);
    }

    public UserException(Integer code, String message) {
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
