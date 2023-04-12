package com.github.excel.adapter;

import com.alibaba.excel.annotation.ExcelProperty;
import com.github.excel.adapter.base.BaseExcelValueAdapter;
import org.springframework.stereotype.Component;

/**
 * @author : y1
 * @className : ExcelPropertyHandler
 * @date: 2023/4/7 16:22
 * @description :
 */
@Component
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
