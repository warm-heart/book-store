package com.book.enums;

import lombok.Getter;


@Getter
public enum UserEnum {
    USER_UPDATE_FAIL(0, "更新用户信息失败"),
    USER_DELETE_FAIL(1, "删除用户信息失败"),
    USER_SAVE_FAIL(2, "保存用户信息失败"),
    USER_NOT_EXIST(4, "用户不存在"),

    LOGIN_FAIL(5, "登录失败, 登录信息不正确"),;


    private Integer code;

    private String message;

    UserEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
