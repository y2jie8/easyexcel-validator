package com.github.excel.common;

import com.github.excel.adapter.base.BaseExcelValueAdapter;
import com.github.excel.utils.CastUtils;
import com.github.excel.utils.ExcelValueCommonUtils;
import com.google.common.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * excel字段处理器
 *
 * @author : y1
 * @className : ExcelAnnotationHandler
 * @date: 2023/4/7 14:19
 * @description : excel注解处理器
 */
public abstract class ExcelAnnotationHandler<A extends Annotation> {
    public ExcelAnnotationHandler() {
        TypeToken<A> typeToken = new TypeToken<A>(getClass()) {
        };
        this.clazzAnnotation = CastUtils.cast(typeToken.getRawType());
        this.baseExcelValueHandler = CastUtils.cast(ExcelValueCommonUtils.getValueHandler(this.clazzAnnotation));
    }

    @Getter
    private final Class<A> clazzAnnotation;
    @Getter
    private final BaseExcelValueAdapter<A> baseExcelValueHandler;

    /**
     * 必填字段符号例：*,# 默认 *
     */
    @Getter
    @Setter
    protected String regex;

    protected Boolean isAnnotationPresent(Field field) {
        return field.isAnnotationPresent(clazzAnnotation);
    }

    protected A getAnnotation(Field field) {
        return field.getAnnotation(clazzAnnotation);
    }

    protected String replace(String value) {
        return "【".concat(value.replaceAll("\\".concat(this.getRegex()), "").concat("】"));
    }

    /**
     * 获取字段ExcelProperty的Value
     *
     * @param field
     * @return
     */
    protected abstract String getExcelPropertyValue(Field field);
}