package com.github.excel.handler;

import com.alibaba.excel.EasyExcel;
import com.github.excel.dto.ExcelErrorDto;
import com.github.excel.factory.ReadListenerFactory;
import com.github.excel.factory.enums.ReadListenerEnum;
import com.github.excel.listener.BaseReadListener;
import lombok.SneakyThrows;
import org.springframework.core.io.InputStreamSource;

import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

/**
 * excel导入处理器
 *
 * @author : y1
 * @className : ReadListenerHandler
 * @date: 2023/3/17 14:12
 * @description : excel导入处理器
 */
public class ReadListenerHandler {

    private ReadListenerHandler(Builder builder) {
        this.listener = builder.listener;
        this.inputStream = builder.inputStream;
        Optional.ofNullable(builder.headRowNumber).ifPresentOrElse(number -> this.headRowNumber = number, () -> this.headRowNumber = 2);
        Optional.ofNullable(builder.regex).ifPresentOrElse(regex -> this.regex = regex, () -> this.regex = "*");
        Optional.ofNullable(builder.excelCheckSize).ifPresentOrElse(excelCheckSize -> this.excelCheckSize = excelCheckSize, () -> this.excelCheckSize = 10000);
        Optional.ofNullable(builder.isHibernateValid).ifPresentOrElse(isHibernateValid -> this.isHibernateValid = isHibernateValid, () -> this.isHibernateValid = Boolean.FALSE);
        listener.setRegex(this.regex);
        listener.setExcelCheckSize(this.excelCheckSize);
        listener.setIsHibernateValid(this.isHibernateValid);
    }

    /**
     * 监听器
     */
    private final BaseReadListener<?, ?> listener;
    /**
     * 文件流
     */
    private final InputStream inputStream;
    /**
     * 读取行数 默认 2
     */
    private Integer headRowNumber;
    /**
     * 必填字符 默认 *
     */
    private String regex;
    /**
     * 验证数据阀值 默认1w
     */
    private Integer excelCheckSize;
    /**
     * 是否使用hibernate valid验证
     */
    private Boolean isHibernateValid;

    /**
     * 是否可以保存
     *
     * @return true 可以 false 不可以
     */
    public boolean isSuccess() {
        return listener.isSuccess();
    }

    public List<ExcelErrorDto> getErrorDataList() {
        return listener.getErrorDataList();
    }

    /**
     * 构建者模式
     *
     * @return
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 内部处理
     */
    private void handle() {
        EasyExcel.read(this.inputStream, getActualTypeArgument(this.listener.getClass()), this.listener).sheet().headRowNumber(this.headRowNumber).doRead();
    }

    /**
     * 对外处理
     */
    public ReadListenerHandler read() {
        handle();
        return this;
    }


    /*
     * 获取泛型类Class对象，不是泛型类则返回null
     */
    public static Class<?> getActualTypeArgument(Class<?> clazz) {
        Class<?> entitiClass = null;
        Type genericSuperclass = clazz.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
            if (actualTypeArguments != null && actualTypeArguments.length > 0) {
                entitiClass = (Class<?>) actualTypeArguments[0];
            }
        }
        return entitiClass;
    }

    /**
     * 处理器子类构建者
     */
    public static class Builder {
        private BaseReadListener<?, ?> listener;
        private InputStream inputStream;
        private Integer headRowNumber;
        private String regex;
        private Integer excelCheckSize;
        private Boolean isHibernateValid;

        public Builder listener(Class<? extends BaseReadListener<?, ?>> clazz) {
            this.listener = ReadListenerFactory.getInstance(clazz);
            return this;
        }


        public Builder file(InputStream inputStream) {
            this.inputStream = inputStream;
            return this;
        }

        @SneakyThrows
        public Builder file(InputStreamSource file) {
            this.inputStream = file.getInputStream();
            return this;
        }

        public Builder headRowNumber(Integer headRowNumber) {
            this.headRowNumber = headRowNumber;
            return this;
        }

        public Builder headRowNumber() {
            this.headRowNumber = 2;
            return this;
        }

        public Builder regex(String regex) {
            this.regex = regex;
            return this;
        }

        public Builder excelCheckSize(Integer excelCheckSize) {
            this.excelCheckSize = excelCheckSize;
            return this;
        }

        public Builder isHibernateValid(Boolean isHibernateValid) {
            this.isHibernateValid = isHibernateValid;
            return this;
        }

        public ReadListenerHandler build() {
            return new ReadListenerHandler(this);
        }
    }
}
