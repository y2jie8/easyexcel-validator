package com.github.excel.adapter;

import com.alibaba.excel.annotation.ExcelProperty;
import com.github.excel.adapter.base.BaseExcelValueAdapter;

public class ExcelPropertyExcelAdapter implements BaseExcelValueAdapter<ExcelProperty> {
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
