package com.github.excel.provider.impl;

import com.alibaba.excel.annotation.ExcelProperty;
import com.github.excel.provider.ExcelValueProvider;

public class ExcelPropertyProviderImpl implements ExcelValueProvider<ExcelProperty> {
    /**
     * 获取excel注解值
     *
     * @param annotation
     * @return
     */
    @Override
    public String getExcelValue(ExcelProperty annotation) {
        return annotation.value()[0];
    }
}
