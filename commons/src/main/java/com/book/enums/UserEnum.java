package com.book.enums;

import lombok.Getter;


@Getter
public enum UserEnum {
    BOOK_UPDATE_FAIL(0, "更新图书失败"),
    BOOK_DELETE_FAIL(1, "删除图书失败"),
    BOOK_SAVE_FAIL(2, "保存图书失败"),
    USER_NOT_EXIST(3, "用户不存在"),
    
    LOGIN_FAIL(25, "登录失败, 登录信息不正确"),;


    private Integer code;

    private String message;

    UserEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
