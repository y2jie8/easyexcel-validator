package com.github.excel.factory;


import cn.hutool.core.util.ReflectUtil;
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
    public static BaseReadListener<?, ?> getInstance(Class<? extends BaseReadListener<?, ?>> t) {
        return ReflectUtil.newInstance(t);
    }
}
