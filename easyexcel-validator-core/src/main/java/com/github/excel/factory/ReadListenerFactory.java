package com.github.excel.factory;


import com.github.excel.factory.enums.ReadListenerEnum;
import com.github.excel.listener.BaseReadListener;

/**
 * 导入工厂
 *
 * @author : y1
 * @className : ReadListenerFactory
 * @date: 2023/3/17 13:44
 * @description : 导入工厂
 */
public final class ReadListenerFactory {
    private ReadListenerFactory() {
    }

    /**
     * 根据类创建监听器
     *
     * @param t
     * @return
     */
    public static BaseReadListener<?,?> getInstance(Class<?> t) {
        return ReadListenerEnum.getReadListenerEnum(t).getListener();
    }

    /**
     * 根据枚举创建监听器
     *
     * @param readListenerEnum
     * @return
     */
    public static BaseReadListener<?,?> getInstance(ReadListenerEnum readListenerEnum) {
        return readListenerEnum.getListener();
    }

}
