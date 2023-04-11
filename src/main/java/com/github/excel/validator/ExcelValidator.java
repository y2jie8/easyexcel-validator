package com.github.excel.validator;

import com.github.excel.utils.CastUtils;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.HibernateValidator;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

/**
 * hibernate检查器
 *
 * @author : y1
 * @className : ExcelValidator
 * @date: 2023/4/10 11:02
 * @description : hibernate检查器
 */
@Slf4j
public class ExcelValidator<T> {
    private volatile static ExcelValidator<?> singleton;
    private static final Validator VALIDATOR = Validation.byProvider(HibernateValidator.class).configure().buildValidatorFactory().getValidator();

    private ExcelValidator() {
    }

    public static <T> ExcelValidator<T> getSingleton(Class<T> clazz) {
        if (singleton == null) {
            synchronized (ExcelValidator.class) {
                if (singleton == null) {
                    singleton = new ExcelValidator<T>();
                }
            }
        }
        return CastUtils.cast(singleton);
    }

    /**
     * 验证数据
     *
     * @param data
     * @return
     */
    public Set<ConstraintViolation<T>> valid(T data) {
        return VALIDATOR.validate(data);
    }
}
