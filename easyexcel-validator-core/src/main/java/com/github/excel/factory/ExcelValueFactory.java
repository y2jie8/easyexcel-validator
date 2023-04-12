package com.github.excel.factory;

import cn.hutool.core.util.ServiceLoaderUtil;
import com.github.excel.adapter.base.BaseExcelValueProvider;
import com.github.excel.utils.CastUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * exce注解选用工厂
 *
 * @author : y1
 * @className : BaseExcelValueCommon
 * @date: 2023/4/7 16:42
 * @description : exce注解选用工厂
 */
public class ExcelValueFactory {
    private static final Map<Class<? extends Annotation>, BaseExcelValueProvider<? extends Annotation>> map = new HashMap<>();

    private ExcelValueFactory() {
    }

    /**
     * @param clazz
     * @return
     */
    public static BaseExcelValueProvider<? extends Annotation> getValueHandler(Class<? extends Annotation> clazz) {
        if (map.isEmpty()) {
            fillMap();
        }
        return Optional.ofNullable(map.get(clazz)).orElseThrow(() -> new RuntimeException("无法获取excel注解处理器Bean"));
    }

    /**
     * 填充Map
     */
    private static void fillMap() {
        List<BaseExcelValueProvider<? extends Annotation>> excelValueProviderList = CastUtils.cast(ServiceLoaderUtil.loadList(BaseExcelValueProvider.class));
        for (BaseExcelValueProvider<? extends Annotation> value : excelValueProviderList) {
            Type genericType = value.getClass().getGenericInterfaces()[0];
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            Class<? extends Annotation> genericClass = CastUtils.cast(parameterizedType.getActualTypeArguments()[0]);
            map.put(genericClass, value);
        }
    }
}
