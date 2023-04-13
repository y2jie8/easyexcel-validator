package com.github.excel.provider;

import java.lang.annotation.Annotation;

/**
 * Excel获取注解值Provider
 *
 * @author : y1
 * @className : ExcelValueProvider
 * @date: 2023/4/7 16:27
 * @description : Excel接口
 */
public interface ExcelValueProvider<A extends Annotation> {
    /**
     * 获取excel注解值
     *
     * @param annotation
     * @return
     */
    String getExcelValue(A annotation);
}
