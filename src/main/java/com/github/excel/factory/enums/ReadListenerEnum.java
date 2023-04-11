package com.github.excel.factory.enums;

import com.github.excel.listener.BaseReadListener;
import lombok.SneakyThrows;

import java.util.Arrays;

/**
 * 导入监听器枚举
 *
 * @author : y1
 * @className : ReadListenerEnum
 * @date: 2023/3/17 14:47
 * @description : 导入监听器枚举
 */
public enum ReadListenerEnum {


    ;

    private final Class<?> paramClass;
    private final Class<?> listenerClass;


    public Class<?> getParamClass() {
        return paramClass;
    }

    ReadListenerEnum(Class<?> paramClass, Class<?> listenerClass) {
        this.paramClass = paramClass;
        this.listenerClass = listenerClass;
    }


    @SneakyThrows
    public BaseReadListener<?,?> getListener() {
        return (BaseReadListener<?,?>) this.listenerClass.getDeclaredConstructor().newInstance();
    }

    /**
     * 根据入参获取监听器
     *
     * @param paramClass
     * @return
     */
    public static ReadListenerEnum getReadListenerEnum(Class<?> paramClass) {
        return Arrays.stream(ReadListenerEnum.values())
                .filter(i -> i.getParamClass().equals(paramClass))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到对应监听器"));
    }
}
