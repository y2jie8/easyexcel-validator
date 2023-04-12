package com.github.excel.annotation;

import java.lang.annotation.*;

/**
 * 唯一行字段注解
 *
 * @author : y1
 * @className : OnlyKey
 * @date: 2023/3/22 17:08
 * @description : 唯一行字段注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
@Documented
public @interface OnlyKey {
}
