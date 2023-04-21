package com.github.example.listener;

import com.alibaba.excel.annotation.ExcelProperty;
import com.github.example.entity.ExampleExcel;
import com.github.example.excel.ExampleExcelParam;
import com.github.example.service.ExampleExcelService;
import com.github.excel.listener.BaseReadListener;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 示例导入
 *
 * @author : y1
 * @className : ExampleExcelListener
 * @date: 2023/4/12 09:59
 * @description : 示例导入
 */
public class ExampleExcelListener extends BaseReadListener<ExampleExcelParam, ExcelProperty> {

    private final ExampleExcelService exampleExcelService;

    public ExampleExcelListener() {
        // 实际使用SpringUtil.getBean()注入需要的Bean
        this.exampleExcelService = new ExampleExcelService() {
        };
    }

    /**
     * 持久化数据库 自定义接口
     *
     * @param collect
     */
    @Override
    protected void execute(List<ExampleExcelParam> collect) {
        List<ExampleExcel> list = exampleExcelService.list;
        Map<String, String> nameMaps = exampleExcelService.getNameMaps();
        Map<String, String> idNameMaps = exampleExcelService.getIdNameMaps();

        List<ExampleExcel> saveData = new ArrayList<>();
        for (ExampleExcelParam excelParam : collect) {
            ExampleExcel excel = new ExampleExcel();
            BeanUtils.copyProperties(excelParam, excel);
            super.checkItselfSetCommon(collect, ExampleExcelParam::getCreatorName, excelParam.getCreatorName(), ExampleExcelParam::getCreatorName);
            super.checkSetCommon(list, ExampleExcel::getInitialsName, excelParam.getCreatorName(), ExampleExcelParam::getCreatorName);
            super.checkMapCommon(nameMaps, excelParam.getInitials(), excel::setInitialsName, ExampleExcelParam::getInitials);
            super.checkMapCommon(idNameMaps, excelParam.getCreatorName(), excel::setCreatorId, ExampleExcelParam::getCreatorName);
            super.selfIncreasing();
            saveData.add(excel);
        }
        ifPresent(super.isSuccess(), () -> {
            exampleExcelService.saveBatch(saveData);
        });
    }
}
