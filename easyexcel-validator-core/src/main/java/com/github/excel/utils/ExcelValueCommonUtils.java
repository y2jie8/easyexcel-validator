package com.github.excel.utils;

import cn.hutool.extra.spring.SpringUtil;
import com.github.excel.adapter.ExcelPropertyExcelAdapter;
import com.github.excel.adapter.base.BaseExcelValueAdapter;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * exce注解公共类
 *
 * @author : y1
 * @className : BaseExcelValueCommon
 * @date: 2023/4/7 16:42
 * @description : exce注解公共类
 */
public class ExcelValueCommonUtils {
    private static final Map<Class<? extends Annotation>, BaseExcelValueAdapter<?>> map = new HashMap<>();

    private ExcelValueCommonUtils() {
    }

    /**
     * @param clazz
     * @return
     */
    public static BaseExcelValueAdapter<? extends Annotation> getValueHandler(Class<? extends Annotation> clazz) {
        if (map.isEmpty()) {
            fillMap();
        }
        return Optional.ofNullable(map.get(clazz)).orElseThrow(() -> new RuntimeException("无法获取excel注解处理器Bean"));
    }

    /**
     * 填充Map
     */
    private static void fillMap() {
        Map<String, BaseExcelValueAdapter<?>> beansOfType = CastUtils.cast(SpringUtil.getBeansOfType(BaseExcelValueAdapter.class));
        for (BaseExcelValueAdapter<?> value : beansOfType.values()) {
            Type genericType = ExcelPropertyExcelAdapter.class.getGenericInterfaces()[0];
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            Class<? extends Annotation> genericClass = CastUtils.cast(parameterizedType.getActualTypeArguments()[0]);
            map.put(genericClass, value);
        }
    }
}
