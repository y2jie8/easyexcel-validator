package com.github.excel.adapter.base;

import java.lang.annotation.Annotation;

/**
 * Excel接口
 *
 * @author : y1
 * @className : BaseExcelValue
 * @date: 2023/4/7 16:27
 * @description : Excel接口
 */
public interface BaseExcelValueProvider<A extends Annotation> {
    /**
     * 获取excel注解值
     *
     * @param annotation
     * @return
     */
    String getExcelValue(A annotation);
}
