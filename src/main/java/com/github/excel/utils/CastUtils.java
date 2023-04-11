package com.github.excel.utils;

/**
 * 对象转回泛型
 *
 * @author : y1
 * @className : CastUtils
 * @date: 2023/4/11 16:19
 * @description : 对象转回泛型
 */
public interface CastUtils {

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object object) {
        return (T) object;
    }
}
