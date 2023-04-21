package com.github.excel.engine;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.github.excel.annotation.OnlyKey;
import com.github.excel.dto.ExcelInDto;
import com.github.excel.processor.ExcelAnnotationProcessor;
import com.github.excel.dto.ExcelErrorDto;
import com.github.excel.utils.CastUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * 服务Excel监听器核心模块
 *
 * @author : y1
 * @className : ReadListenerEngine
 * @date: 2023/3/22 17:01
 * @description : 服务Excel监听器核心模块
 */
public abstract class ReadListenerEngine<T extends ExcelInDto, A extends Annotation> extends ExcelAnnotationProcessor<A> {
    private final Map<String, ExcelErrorDto> errorDataMap = new TreeMap<>();

    @Getter
    private final Map<String, Field> fieldMap;
    @Getter
    private final Class<T> clazz;
    private final Field[] fields;
    /**
     * 单次检查excel行数
     */
    @Setter
    @Getter
    private Integer excelCheckSize;
    /**
     * 是否开启hibernate-valid默认不开启
     */
    @Getter
    @Setter
    private Boolean isHibernateValid;

    public ReadListenerEngine() {
        super();
        this.clazz = CastUtils.cast(((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
        this.fields = ReflectUtil.getFieldsDirectly(clazz, false);
        this.fieldMap = Arrays.stream(fields).collect(Collectors.toMap(Field::getName, Function.identity()));
    }


    protected abstract Integer getIndex();

    protected abstract void setIndex(@NonNull Integer index);

    /**
     * 自增
     */
    protected abstract void selfIncreasing();


    public List<ExcelErrorDto> getErrorDataList() {
        return errorDataMap.values().stream().filter(i -> Validator.isNotEmpty(i.getErrorMessage())).collect(Collectors.toList());
    }

    /**
     * 新增异常
     *
     * @param errorDto
     */
    protected void addErrorData(ExcelErrorDto errorDto) {
        fillErrorData(errorDto.getRow(), errorDto);
    }

    /**
     * 新增异常
     *
     * @param row
     * @param onlyKey
     * @param errorMsg
     */
    protected void addErrorData(String row, String onlyKey, String errorMsg) {
        ExcelErrorDto errorDto = new ExcelErrorDto(row, onlyKey, errorMsg);
        this.fillErrorData(row, errorDto);
    }

    /**
     * 新增异常
     *
     * @param row
     * @param errorMsg
     */
    protected void addErrorData(String row, String errorMsg) {
        ExcelErrorDto errorDto = new ExcelErrorDto(row, null, errorMsg);
        this.fillErrorData(row, errorDto);
    }

    /**
     * 新增异常
     *
     * @param row
     * @param errorMsg
     */
    protected void addErrorData(String row, Func1<T, ?> column, String errorMsg) {
        ExcelErrorDto errorDto = new ExcelErrorDto(row, null, this.getExcelPropertyValue(column) + errorMsg);
        this.fillErrorData(row, errorDto);
    }

    /**
     * 新增异常
     *
     * @param startRow
     * @param endRow
     * @param errorMsg
     */
    protected void addErrorDataRow(String startRow, String endRow, String onlyKey, String errorMsg) {
        String row = startRow + "-" + endRow;
        ExcelErrorDto errorDto = new ExcelErrorDto(row, Validator.isNotEmpty(onlyKey) ? onlyKey : null, errorMsg);
        Optional.ofNullable(errorDataMap.get(row)).ifPresentOrElse(i -> {
            ifPresent(!i.getErrorMessage().contains(errorMsg), () -> {
                i.setErrorMessage(i.getErrorMessage().concat(errorDto.getErrorMessage()));
            });
        }, () -> errorDataMap.put(row, errorDto));
    }


    /**
     * 处理数据
     *
     * @param row
     * @param errorDto
     */
    private void fillErrorData(String row, ExcelErrorDto errorDto) {
        Optional.ofNullable(errorDataMap.get(row)).ifPresentOrElse(i -> {
            i.setErrorMessage(i.getErrorMessage().concat(errorDto.getErrorMessage()));
            ifPresent(Validator.isNotEmpty(errorDto.getOnlyKey()),
                    () -> i.setOnlyKey(Validator.isNotEmpty(i.getOnlyKey()) ? i.getOnlyKey().concat(errorDto.getOnlyKey()) : errorDto.getOnlyKey()));
        }, () -> errorDataMap.put(row, errorDto));
    }

    /**
     * 错误信息
     *
     * @param key
     * @return
     */
    protected <V> String exists(V key) {
        return String.valueOf(key).concat("已经存在;");
    }

    /**
     * 错误信息
     *
     * @param key
     * @return
     */
    protected <V> String tableExists(V key) {
        return String.valueOf(key).concat("表格中已经存在;");
    }

    /**
     * 错误信息
     *
     * @param key
     * @return
     */
    protected <V> String nonExists(V key) {
        return String.valueOf(key).concat("不存在;");
    }

    protected String nonExists() {
        return "【黄色部分】需不填或全部填写;";
    }

    /**
     * 检验数据字典是否存在
     *
     * @param map    字典集合
     * @param key    值
     * @param action 存在执行function
     */
    protected <K, V> void checkMapCommon(Map<K, V> map, K key, Consumer<V> action, Func1<T, ?> column) {
        ifPresent(Validator.isNotEmpty(key), () -> {
            Optional.ofNullable(map.get(key)).ifPresentOrElse(action, () -> {
                this.addErrorData(getIndex().toString(), column, nonExists(key));
            });
        });
    }

    /**
     * 检验数据字典是否存在
     *
     * @param map           字典集合
     * @param key           值
     * @param action        存在执行function
     * @param typeConverter 转换器
     */
    protected <K, V, R> void checkMapCommon(Map<K, V> map, K key, Consumer<R> action, Function<V, R> typeConverter, Func1<T, ?> column) {
        ifPresent(Validator.isNotEmpty(key), () -> {
            Optional.ofNullable(map.get(key)).ifPresentOrElse(item -> action.accept(typeConverter.apply(item)), () -> {
                this.addErrorData(getIndex().toString(), column, nonExists(key));
            });
        });
    }


    /**
     * 检验数据字典是否存在
     * 存在返回值
     * 不存在返回null
     *
     * @param map 字典集合
     * @param key 值
     * @return V
     */
    protected <K, V> V checkMapCommon(Map<K, V> map, K key, Func1<T, ?> column) {
        return Optional.ofNullable(map.get(key)).orElseGet(() -> {
            this.addErrorData(getIndex().toString(), column, nonExists(key));
            return null;
        });
    }

    /**
     * 检验数据字典是否存在
     *
     * @param map          字典集合
     * @param key          值
     * @param action       存在直接执行action
     * @param defaultValue 不存在 使用默认值 调用action 传递defaultValue
     */
    protected <K, V> void checkMapCommon(Map<K, V> map, K key, Consumer<V> action, V defaultValue, Func1<T, ?> column) {
        ifPresent(Validator.isNotEmpty(key), () -> {
            Optional.ofNullable(map.get(key)).ifPresentOrElse(action, () -> {
                this.addErrorData(getIndex().toString(), column, nonExists(key));
            });
        }, () -> action.accept(defaultValue));
    }

    /**
     * 检验数据字典是否存在
     *
     * @param supplier 函数
     * @param key      值
     * @param action   存在执行function
     */
    protected <K, V> void checkMapCommon(Supplier<Map<K, V>> supplier, K key, Consumer<V> action, Func1<T, ?> column) {
        Optional.ofNullable(supplier.get().get(key)).ifPresentOrElse(action, () -> {
            this.addErrorData(getIndex().toString(), column, nonExists(key));
        });
    }

    /**
     * 查询是否存在唯一
     *
     * @param list
     * @param mapper
     * @param key
     */
    protected <E> void checkSetCommon(List<E> list, Function<E, String> mapper, String key, Func1<T, ?> column) {
        ifPresent(list.stream().map(mapper).collect(Collectors.toSet()).contains(key), () -> this.addErrorData(getIndex().toString(), column, exists(key)));
    }


    /**
     * 查询是否存在唯一
     *
     * @param list
     * @param mapper
     * @param key
     */
    protected <V> void checkItselfSetCommon(List<T> list, Function<T, V> mapper, V key, Func1<T, ?> column) {
        ifPresent(list.stream().map(mapper).collect(Collectors.toList()).stream().filter(i -> i.equals(key)).count() > 1, () -> this.addErrorData(getIndex().toString(), column, tableExists(key)));
    }

    /**
     * 查询是否存在唯一
     *
     * @param list
     * @param key
     */
    protected <E> void checkItselfSetCommon(Collection<E> list, E key, Func1<T, ?> column) {
        ifPresent(list.stream().filter(i -> i.equals(key)).count() > 1, () -> this.addErrorData(getIndex().toString(), column, tableExists(key)));
    }

    /**
     * List转Map
     *
     * @param list
     * @param keyMapper
     * @param valueMapper
     * @param <E>
     * @param <K>
     * @param <U>
     * @return
     */
    protected <E, K, U> Map<K, U> toMap(List<E> list, Function<? super E, ? extends K> keyMapper, Function<? super E, ? extends U> valueMapper) {
        return list.stream().collect(Collectors.toMap(keyMapper, valueMapper));
    }

    /**
     * List转Map
     *
     * @param list
     * @param keyMapper
     * @param valueMapper
     * @param mergeFunction
     * @param <E>
     * @param <K>
     * @param <U>
     * @return
     */
    protected <E, K, U> Map<K, U> toMap(List<E> list, Function<? super E, ? extends K> keyMapper, Function<? super E, ? extends U> valueMapper, BinaryOperator<U> mergeFunction) {
        return list.stream().collect(Collectors.toMap(keyMapper, valueMapper, mergeFunction));
    }

    /**
     * 做事函数
     */
    @FunctionalInterface
    public interface DoSomething {
        /**
         * 执行
         */
        void doIt();

    }

    /**
     * 条件=true
     * 运行函数
     *
     * @param condition
     * @param action
     */
    protected void ifPresent(boolean condition, DoSomething action) {
        if (condition) {
            action.doIt();
        }
    }

    /**
     * 条件=true
     * 运行函数
     *
     * @param condition
     * @param action
     */
    protected <E> E ifPresent(boolean condition, Supplier<E> action) {
        if (condition) {
            return action.get();
        }
        return null;
    }

    /**
     * 条件=true
     * 运行函数
     *
     * @param condition
     * @param action
     */
    protected <E> E ifPresent(boolean condition, Supplier<E> action, E defaultValue) {
        if (condition) {
            return action.get();
        } else {
            return defaultValue;
        }
    }

    /**
     * 条件rue
     * 运行action函数
     * 条件false
     * 运行otherAction
     *
     * @param condition
     * @param action
     * @param otherAction
     */
    protected void ifPresent(boolean condition, DoSomething action, DoSomething otherAction) {
        if (condition) {
            action.doIt();
        } else {
            otherAction.doIt();
        }
    }

    /**
     * 分隔字符串
     *
     * @param value
     * @return
     */
    protected List<String> split(String value) {
        return ifPresent(Validator.isNotEmpty(value), () -> {
            String[] split = value.split(",");
            if (split.length < 1) {
                split = value.split("，");
            }
            return CollUtil.toList(split);
        }, CollUtil.newArrayList());
    }

    /**
     * 生成新集合
     *
     * @param e
     * @param size
     * @param <E>
     * @return
     */
    @SneakyThrows
    protected <E> List<E> toList(Class<E> e, int size) {
        ArrayList<E> list = CollUtil.newArrayList();
        for (int i = 0; i < size; i++) {
            list.add(e.getDeclaredConstructor().newInstance());
        }
        return list;
    }


    /**
     * 将list的每个值通过转换器,放到每个E对象的某个函数
     *
     * @param typeConverter
     * @param consumer
     * @param <K>
     * @param <R>
     * @param <E>
     */
    private <K, R, E> void setObjectsWithListCommon(K key, E entity, Function<K, R> typeConverter, BiConsumer<E, R> consumer) {
        R value = typeConverter.apply(key);
        consumer.accept(entity, value);
    }

    /**
     * 将list的每个值通过转换器,放到每个E对象的某个函数
     *
     * @param list
     * @param entityList
     * @param typeConverter
     * @param consumer
     * @param <K>
     * @param <R>
     * @param <E>
     */
    protected <K, R, E> void setObjectsWithListCommon(List<K> list, List<E> entityList, Function<K, R> typeConverter, BiConsumer<E, R> consumer) {
        for (int i = 0; i < list.size(); i++) {
            setObjectsWithListCommon(list.get(i), entityList.get(i), typeConverter, consumer);
        }
    }

    /**
     * 批处理
     *
     * @param list
     * @param entityList
     * @param setterPairs
     * @param <K>
     * @param <E>
     */
    private <K, E, R> void setObjectsWithListCommon(List<List<K>> list, List<E> entityList, List<Pair<BiConsumer<E, R>, Function<K, R>>> setterPairs) {
        for (int i = 0; i < entityList.size(); i++) {
            for (int j = 0; j < list.size(); j++) {
                setObjectsWithListCommon(list.get(j).get(i), entityList.get(i), setterPairs.get(j).getValue(), setterPairs.get(j).getKey());
            }
        }
    }

    /**
     * 批处理
     *
     * @param list
     * @param clazz
     * @param setterPairs
     * @param <K>
     * @param <E>
     */
    protected <K, E, R> List<E> setObjectsWithListCommon(List<List<K>> list, Class<E> clazz, List<Pair<BiConsumer<E, R>, Function<K, R>>> setterPairs) {
        Set<Integer> collect = list.stream().map(List::size).collect(Collectors.toSet());
        if (collect.size() > 1) {
            this.addErrorData(getIndex().toString(), nonExists());
        }
        List<E> entityList = toList(clazz, list.get(0).size());
        setObjectsWithListCommon(list, entityList, setterPairs);
        return entityList;
    }

    protected <E> List<Pair<BiConsumer<E, Object>, Function<String, Object>>> newListPair(Class<E> e) {
        return CollUtil.newArrayList();
    }

    /**
     * 对象所有值拼接
     *
     * @param clazz
     * @param fieIdNames
     * @return
     */
    protected String toKey(T clazz, String... fieIdNames) {
        Field[] fields = ReflectUtil.getFieldsDirectly(clazz.getClass(), false);
        Map<String, Field> collect = Arrays.stream(fields).filter(i -> Arrays.stream(fieIdNames).noneMatch(j -> j.contentEquals(i.getName()))).collect(Collectors.toMap(Field::getName, Function.identity()));
        StringBuilder builder = new StringBuilder();
        collect.forEach((k, v) -> {
            builder.append(Optional.ofNullable(ReflectUtil.getFieldValue(clazz, v)).orElse(""));
        });
        return builder.toString();
    }

    /**
     * 检查集合是否完全相同
     *
     * @param excelParamsList
     * @param fieIdNames
     */
    protected void checkFieldValue(Collection<List<T>> excelParamsList, String... fieIdNames) {
        if (excelParamsList.size() <= 1) {
            return;
        }
        List<T> excelParams = excelParamsList.stream().flatMap(Collection::stream).collect(Collectors.toList());
        Map<String, Field> filterFieldMap = Arrays.stream(this.fields).filter(i -> Arrays.stream(fieIdNames).noneMatch(j -> j.contentEquals(i.getName()))).collect(Collectors.toMap(Field::getName, Function.identity()));
        excelParams.stream().findFirst().ifPresent(e -> {
            Map<String, Object> firstFields = new HashMap<>();
            AtomicReference<String> onlyKey = new AtomicReference<>();
            filterFieldMap.keySet().forEach(k -> {
                Object fieldValue = ReflectUtil.getFieldValue(e, k);
                firstFields.put(k, fieldValue);
                if (filterFieldMap.get(k).isAnnotationPresent(OnlyKey.class)) {
                    onlyKey.set(fieldValue.toString());
                }
            });
            Optional<T> minE = excelParams.stream().min(Comparator.comparing(ExcelInDto::getIndex));
            Optional<T> maxE = excelParams.stream().max(Comparator.comparing(ExcelInDto::getIndex));
            AtomicReference<Integer> min = new AtomicReference<>(1);
            AtomicReference<Integer> max = new AtomicReference<>(1);
            minE.ifPresent(item -> min.set(item.getIndex()));
            maxE.ifPresent(item -> max.set(item.getIndex()));
            for (int i = 1; i < excelParams.size(); i++) {
                T val = excelParams.get(i);
                filterFieldMap.values().forEach(v -> {
                    Object fieldValue = ReflectUtil.getFieldValue(val, v);
                    Object o = firstFields.get(v.getName());
                    if (!ObjectUtil.equal(o, fieldValue)) {
                        if (isAnnotationPresent(v)) {
                            String value = super.getExcelPropertyValue(v);
                            value = super.replace(value);
                            this.addErrorDataRow(min.get().toString(), max.get().toString(), onlyKey.get(), value.concat("字段主表维护数据不一致;"));
                        }
                    }
                });
            }
        });
    }

    /**
     * 根据lambda方法获取字段上注解的方法值
     *
     * @param column
     * @return
     */
    @Override
    protected String getExcelPropertyValue(Func1<?, ?> column) {
        String fieldName = LambdaUtil.getFieldName(column);
        Field field = this.fieldMap.get(fieldName);
        return super.replace(super.getExcelPropertyValue(field));
    }
}
