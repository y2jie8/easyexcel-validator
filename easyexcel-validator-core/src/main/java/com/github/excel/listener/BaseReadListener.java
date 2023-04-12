package com.github.excel.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ReflectUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.metadata.CellExtra;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.read.metadata.holder.AbstractReadHolder;
import com.github.excel.annotation.OnlyKey;
import com.github.excel.common.ReadListenerCommon;
import com.github.excel.function.LambdaQueryFunction;
import com.github.excel.validator.ExcelValidator;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintViolation;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 读取Excel文件监听基类
 *
 * @author : y1
 * @className : BaseReadListener
 * @date: 2023/3/16 13:49
 * @description : 读取Excel文件监听基类
 */
@Slf4j
public abstract class BaseReadListener<T, A extends Annotation> extends ReadListenerCommon<T, A> implements ReadListener<T>, LambdaQueryFunction {
    /**
     * 缓存的数据
     */
    private final List<T> cachedDataList = new ArrayList<>();

    protected Integer index = -1;

    /**
     * 自增
     */
    @Override
    protected void selfIncreasing() {
        index++;
    }

    @Override
    protected Integer getIndex() {
        return index;
    }


    @Override
    protected void setIndex(@NonNull Integer index) {
        this.index = index;
    }

    /**
     * 缓存的数据
     *
     * @return
     */
    public List<T> getCachedDataList() {
        return cachedDataList;
    }

    /**
     * 是否可以保存
     *
     * @return true 可以 false 不可以
     */
    public boolean isSuccess() {
        return getErrorDataList().size() < 1;
    }

    public BaseReadListener() {
        super();
    }


    @Override
    public void onException(Exception e, AnalysisContext analysisContext) throws Exception {
        log.error(e.getMessage());
        if (e instanceof ExcelDataConvertException) {
            throw e;
        }
    }


    /**
     * 这个每一条数据解析都会来调用
     *
     * @param data    one row value. Is is same as {@link AnalysisContext#readRowHolder()}
     * @param context
     */
    @Override
    public void invoke(T data, AnalysisContext context) {
        if (log.isDebugEnabled()) {
            log.info("Parse to a piece of data:{}", data.toString());
        }
        // 跳过空行
        ifPresent(this.nonEmpty(data), () -> {
            // 判断必填项
            validatorField(data, context);
            cachedDataList.add(data);
        });
    }

    /**
     * 验证数据,支持hibernate验证数据
     *
     * @param data
     * @param context
     */
    private void validatorField(T data, AnalysisContext context) {
        dealData(data, context);
        super.ifPresent(super.getIsHibernateValid(), () -> {
            Set<ConstraintViolation<T>> valid = ExcelValidator.getSingleton(super.getClazz()).valid(data);
            for (ConstraintViolation<T> constraintViolation : valid) {
                int finalRowIndex = getExcelIndex(context);
                super.ifPresent(finalRowIndex - 1 >= 0, () -> super.addErrorData(String.valueOf(finalRowIndex), constraintViolation.getMessage()));
            }
        });
    }

    /**
     * 判断必填项
     *
     * @param data
     * @param context
     * @see #dealData(T data, AnalysisContext context)
     */
    @Deprecated
    private void requiredField(T data, AnalysisContext context) {
        Map<String, Field> fieldMap = super.getFieldMap();
        for (Field field : fieldMap.values()) {
            if (isAnnotationPresent(field)) {
                boolean onlyKey = field.isAnnotationPresent(OnlyKey.class);
                String value = getExcelPropertyValue(field);
                if (value.contains(super.getRegex())) {
                    Object fieldValue = ReflectUtil.getFieldValue(data, field);
                    int finalRowIndex = getExcelIndex(context);
                    super.ifPresent(onlyKey, () -> super.ifPresent(Validator.isNotEmpty(fieldValue), () -> super.addErrorData(String.valueOf(finalRowIndex), fieldValue.toString(), EMPTY)));
                    super.ifPresent(Validator.isEmpty(fieldValue), () -> super.ifPresent(finalRowIndex - 1 >= 0, () -> super.addErrorData(String.valueOf(finalRowIndex), replace(value).concat("为必填项;"))));
                }
            }
        }
    }

    /**
     * 新验证,支持hibernate Valid
     *
     * @param data
     * @param context
     */
    private void dealData(T data, AnalysisContext context) {
        Map<String, Field> fieldMap = super.getFieldMap();
        for (Field field : fieldMap.values()) {
            boolean onlyKey = field.isAnnotationPresent(OnlyKey.class);
            String value = getExcelPropertyValue(field);
            if (value.contains(super.getRegex())) {
                Object fieldValue = ReflectUtil.getFieldValue(data, field);
                int finalRowIndex = getExcelIndex(context);
                super.ifPresent(onlyKey, () -> super.ifPresent(Validator.isNotEmpty(fieldValue), () -> super.addErrorData(String.valueOf(finalRowIndex), fieldValue.toString(), EMPTY)));
                super.ifPresent(!super.getIsHibernateValid(), () -> {
                    super.ifPresent(Validator.isEmpty(fieldValue), () -> super.ifPresent(finalRowIndex - 1 >= 0, () -> super.addErrorData(String.valueOf(finalRowIndex), replace(value).concat("为必填项;"))));
                });
            }
        }
    }

    @Override
    public void extra(CellExtra cellExtra, AnalysisContext analysisContext) {

    }

    /**
     * 所有数据解析完成了 都会来调用
     *
     * @param context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (log.isDebugEnabled()) {
            log.debug("All data parsing is complete！");
        }
        if (cachedDataList.isEmpty()) {
            throw new RuntimeException("未读取到Excel数据");
        }
        this.setIndex(((AbstractReadHolder) context.currentReadHolder()).getHeadRowNumber() + 1);
        List<List<T>> excelList = CollUtil.split(cachedDataList, super.getExcelCheckSize());
        excelList.forEach(excelItem -> {
            if (isSuccess()) {
                this.execute(excelItem);
            }
        });
    }

    /**
     * 获取当前读取行数
     *
     * @param context
     * @return
     */
    protected Integer getExcelIndex(AnalysisContext context) {
        return ((AbstractReadHolder) context.currentReadHolder()).getHeadRowNumber() + 1;
    }

    @Override
    public boolean hasNext(AnalysisContext analysisContext) {
        return true;
    }


    /**
     * 自动跳过空白行
     *
     * @param data
     * @return
     */
    private boolean nonEmpty(T data) {
        boolean exist = false;
        String[] fieldName = getFieldName(data);
        for (String string : fieldName) {
            Object fieldValue = ReflectUtil.getFieldValue(data, string);
            if (fieldValue instanceof String) {
                if (Validator.isEmpty(fieldValue)) {
                    exist = true;
                }
            }
            if (!Objects.isNull(fieldValue)) {
                exist = true;
            }
        }
        if (!exist) {
            if (log.isDebugEnabled()) {
                log.warn("The line is ignored,object={}", data);
            }
        }
        return exist;
    }

    /**
     * 获取属性名数组
     */
    private String[] getFieldName(Object o) {
        Field[] fields = o.getClass().getDeclaredFields();
        String[] fieldNames = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            if (isAnnotationPresent(fields[i])) {
                fieldNames[i] = fields[i].getName();
            }
        }
        return fieldNames;
    }


    /**
     * 持久化数据库 自定义接口
     *
     * @param collect
     */
    protected abstract void execute(List<T> collect);

    /**
     * 获取字段ExcelProperty的Value
     *
     * @param field
     * @return
     */
    @Override
    protected String getExcelPropertyValue(Field field) {
        if (isAnnotationPresent(field)) {
            A annotation = super.getAnnotation(field);
            return this.getBaseExcelValueHandler().getExcelValue(annotation);
        }
        return EMPTY;
    }
}