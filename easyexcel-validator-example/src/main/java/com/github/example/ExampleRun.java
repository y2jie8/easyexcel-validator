package com.github.example;

import com.github.example.excel.ExampleExcelParam;
import com.github.example.listener.ExampleExcelListener;
import com.github.excel.handler.ReadListenerHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * 运行
 *
 * @author : y1
 * @className : ExampleRun
 * @date: 2023/4/12 10:00
 * @description : 运行
 */
@Slf4j
public class ExampleRun {
    public static void main(String[] args) throws FileNotFoundException {
        File file = new File("example.xls");
        InputStream in = new FileInputStream(file);
        ReadListenerHandler handler = ReadListenerHandler.builder()
                .listener(ExampleExcelListener.class)
                .file(in)
                .headRowNumber()
                .build()
                .read();
        if (!handler.isSuccess()) {
            log.error("导入失败,错误信息->{}", handler.getErrorDataList());
        }
    }
}
