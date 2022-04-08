package io.mybatis.mapper;

import io.mybatis.mapper.base.EntityMapper;
import io.mybatis.mapper.cursor.CursorMapper;
import io.mybatis.mapper.example.Example;
import io.mybatis.mapper.example.ExampleMapper;
import io.mybatis.mapper.example.ExampleWrapper;
import io.mybatis.mapper.fn.Fn;
import io.mybatis.mapper.fn.FnProvider;
import io.mybatis.provider.Caching;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.UpdateProvider;

import java.io.Serializable;
import java.util.List;

/**
 * 基础 Mapper 方法，可以在此基础上继承覆盖已有方法
 *
 * @param <T> 实体类类型
 * @param <I> 主键类型
 * @author liuzh
 */
public interface BaseMapper<T, I extends Serializable>
    extends EntityMapper<T, I>, ExampleMapper<T, Example<T>>, CursorMapper<T, Example<T>> {

  /**
   * Example 查询封装
   */
  default ExampleWrapper<T, I> wrapper() {
    return new ExampleWrapper<>(BaseMapper.this, example());
  }

  /**
   * 根据主键更新实体中不为空的字段，强制字段不区分是否 null，都更新
   * <p>
   * 当前方法来自 {@link io.mybatis.mapper.fn.FnMapper}，该接口中的其他方法用 {@link ExampleMapper} 也能实现
   *
   * @param entity            实体类
   * @param forceUpdateFields 强制更新的字段，不区分字段是否为 null，通过 {@link Fn#of(Fn...)} 创建 {@link Fn.Fns}
   * @return 1成功，0失败
   */
  @Lang(Caching.class)
  @UpdateProvider(type = FnProvider.class, method = "updateByPrimaryKeySelectiveWithForceFields")
  int updateByPrimaryKeySelectiveWithForceFields(@Param("entity") T entity, @Param("fns") Fn.Fns<T> forceUpdateFields);

  /**
   * 根据指定字段集合查询：field in (fieldValueList)
   * <p>
   * 这个方法是个示例，你也可以使用 Java8 的默认方法实现一些通用方法
   *
   * @param field          字段
   * @param fieldValueList 字段值集合
   * @param <F>            字段类型
   * @return 实体列表
   */
  default <F> List<T> selectByFieldList(Fn<T, F> field, List<F> fieldValueList) {
    Example<T> example = new Example<>();
    example.createCriteria().andIn((Fn<T, Object>) field, fieldValueList);
    return selectByExample(example);
  }

  /**
   * 根据指定字段集合删除：field in (fieldValueList)
   * <p>
   * 这个方法是个示例，你也可以使用 Java8 的默认方法实现一些通用方法
   *
   * @param field          字段
   * @param fieldValueList 字段值集合
   * @param <F>            字段类型
   * @return 实体列表
   */
  default <F> int deleteByFieldList(Fn<T, F> field, List<F> fieldValueList) {
    Example<T> example = new Example<>();
    example.createCriteria().andIn((Fn<T, Object>) field, fieldValueList);
    return deleteByExample(example);
  }

}
