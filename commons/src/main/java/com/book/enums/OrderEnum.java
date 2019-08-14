package com.book.enums;

import lombok.Getter;


@Getter
public enum OrderEnum {

    WAIT(0, "等待支付"),
    SUCCESS(1, "支付成功"),

    ;

    private Integer code;

    private String message;

    OrderEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
