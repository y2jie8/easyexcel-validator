package com.github.example.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.github.excel.dto.ExcelInDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * example导入对象
 *
 * @author : y1
 * @className : ExampleExcelParam
 * @date: 2023/4/12 10:01
 * @description :
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExampleExcelParam extends ExcelInDto {
    @ExcelProperty("姓名首字母")
    private String initials;
    @ExcelProperty("创建人")
    private String creatorName;
}
