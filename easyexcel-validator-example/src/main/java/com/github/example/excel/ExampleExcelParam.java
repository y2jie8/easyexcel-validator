package com.github.example.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * example导入对象
 *
 * @author : y1
 * @className : ExampleExcelParam
 * @date: 2023/4/12 10:01
 * @description :
 */
@Data
public class ExampleExcelParam {
    @ExcelProperty("姓名首字母")
    private String initials;
    @ExcelProperty("创建人")
    private String creatorName;
}
