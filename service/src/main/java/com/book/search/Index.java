package com.zhangchu.datacenter.annotation;


import java.lang.annotation.*;

/**
 * @author wql
 * @Description
 * @create 2020-11-30 18:22
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Index {

    String indexName() default "";
}
