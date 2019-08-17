package com.book.enums;

import lombok.Getter;


@Getter
public enum BookEnum {

    BOOK_UPDATE_FAIL(0, "更新图书失败"),
    BOOK_DELETE_FAIL(1, "删除图书失败"),
    BOOK_SAVE_FAIL(2, "保存图书失败"),
    BOOK_NOT_EXIST(3, "图书不存在"),
    BOOK_CATEGORY_NOT_EXIST(4, "图书类目不存在"),
    BOOK_SEARCH_NOT_EXIST(5, "未找到相关图书"),;

    private Integer code;

    private String message;

    BookEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
