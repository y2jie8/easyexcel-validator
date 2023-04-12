package com.github.excel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Excel返回异常
 *
 * @author : y1
 * @className : ExcelErrorDto
 * @date: 2023/3/22 17:06
 * @description : Excel返回异常
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ExcelErrorDto {

    /**
     * 行数
     */
    @NonNull
    private String row;
    /**
     * 唯一Key
     */
    private String onlyKey;
    /**
     * 失败原因
     */
    @NonNull
    private String errorMessage;
}
