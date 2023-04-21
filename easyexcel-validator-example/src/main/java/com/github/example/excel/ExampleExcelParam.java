package com.github.example.excel;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
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
@ExcelIgnoreUnannotated
public class ExampleExcelParam extends ExcelInDto {
    @ExcelProperty("姓名首字母")
    private String initials;
    @ExcelProperty("创建人")
    private String creatorName;
    @ExcelProperty("标题")
    private String title;
    @ExcelProperty("姓名")
    private String name;
}
