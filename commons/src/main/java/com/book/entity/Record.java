package com.book.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wangqianlong
 * @create 2019-07-30 9:26
 */
@Getter
@Setter
@ToString
public class Record implements Serializable {

    private static final long serialVersionUID = -807028217473192513L;

    private Integer recordId;
    private String userId;
    private String bookId;
    private Integer lendStatus;
    private Date  lendDate;
    private Date createTime;
    private Date updateTime;
}
