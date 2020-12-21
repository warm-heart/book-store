package com.zhangchu.datacenter.es.index.base;

import com.zhangchu.datacenter.utils.SnowIdUtil;

/**
 * @author wql
 * @Description
 * @create 2020-12-03 9:25
 */
public class BaseIndexTemplate {
    /**
     * es自动生成的
     */

    String id;


    /**
     * 排序Id 雪花算法
     */
    Long sortId;

    //todo 启用雪花算法排序
    // public BaseIndexTemplate() {
    //     this.sortId = SnowIdUtil.genId();
    // }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getSortId() {
        return sortId;
    }

    public void setSortId(Long sortId) {
        this.sortId = sortId;
    }
}
