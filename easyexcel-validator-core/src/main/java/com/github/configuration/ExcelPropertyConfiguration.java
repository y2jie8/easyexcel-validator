package com.github.configuration;

import com.github.excel.adapter.ExcelPropertyExcelAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ExcelProperty自动注入
 *
 * @author : y1
 * @className : ExcelPropertyConfiguration
 * @date: 2023/4/12 09:43
 * @description : ExcelProperty自动注入
 */
@Configuration
public class ExcelPropertyConfiguration {
    @Bean
    public ExcelPropertyExcelAdapter getExcelPropertyExcelAdapter() {
        return new ExcelPropertyExcelAdapter();
    }
}
