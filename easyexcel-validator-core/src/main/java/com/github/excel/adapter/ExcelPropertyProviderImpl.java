package com.github.excel.adapter;

import com.alibaba.excel.annotation.ExcelProperty;
import com.github.excel.adapter.base.BaseExcelValueProvider;

public class ExcelPropertyProviderImpl implements BaseExcelValueProvider<ExcelProperty> {
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
