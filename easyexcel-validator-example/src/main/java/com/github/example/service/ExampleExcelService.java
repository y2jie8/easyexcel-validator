package com.github.example.service;

import cn.hutool.core.collection.CollUtil;
import com.github.example.entity.ExampleExcel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 示例service
 *
 * @author : y1
 * @className : ExampleExcelService
 * @date: 2023/4/12 10:04
 * @description : 示例service
 */
public interface ExampleExcelService {
    List<ExampleExcel> list = CollUtil.newArrayList();
    default void deal(){
        ExampleExcel excel = new ExampleExcel();
        excel.setCreatorId("1");
        excel.setInitialsName("张三");
        excel = new ExampleExcel();
        excel.setCreatorId("2");
        excel.setInitialsName("李四");
        list.add(excel);
    }

    default Map<String, String> getNameMaps() {
        HashMap<String, String> map = new HashMap<>();
        map.put("zs", "张三");
        map.put("ls", "李四");
        return map;
    }

    default Map<String, String> getIdNameMaps() {
        HashMap<String, String> map = new HashMap<>();
        map.put("1", "张三");
        map.put("2", "李四");
        return map;
    }

    default boolean saveBatch(List<ExampleExcel> list){
        return true;
    }
}
