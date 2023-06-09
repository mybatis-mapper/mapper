package io.mybatis.mapper.wrapper;


import io.mybatis.mapper.fn.Fn;
import io.mybatis.provider.Caching;
import io.mybatis.provider.EntityColumn;
import io.mybatis.provider.EntityInfoMapper;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.apache.ibatis.session.RowBounds;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 扩展mapper
 * <p>
 *
 * @author dyun
 */
public interface PureWrapperMapper<T, I extends Serializable> extends EntityInfoMapper<T> {

  /**
   * 获取 Wrapper 对象
   *
   * @return Wrapper 对象
   */
  default Wrapper<T> sql() {
    return new Wrapper<>();
  }

  /**
   * 获取 Wrapper 对象 并 Criteria 条件
   *
   * @return Criteria 对象
   */
  default Wrapper.Criteria<T> where() {
    return new Wrapper<T>().where();
  }

  /**
   * 根据 ids 批量查询
   *
   * @param ids 主键列表
   * @return 实体列表
   */
  default List<T> selectByIds(I... ids) {
    String idColumn = entityTable().idColumns().stream().findFirst().map(EntityColumn::column).orElseThrow(RuntimeException::new);
    return selectList(where().in(Fn.column(entityClass(), idColumn), Stream.of(ids).collect(Collectors.toList())).build());
  }

  /**
   * 根据 ids 批量删除
   *
   * @param ids 主键列表
   * @return 1成功，0失败
   */
  default int deleteByIds(I... ids) {
    String idColumn = entityTable().idColumns().stream().findFirst().map(EntityColumn::column).orElseThrow(RuntimeException::new);
    return delete(where().in(Fn.column(entityClass(), idColumn), Stream.of(ids).collect(Collectors.toList())).build());
  }

  /*
   * ========================================rename wrapper method for pure========================================
   */

  /**
   * 保存实体（字段不为空）
   *
   * @param entity 实体类
   * @return 1成功，0失败
   */
  @Lang(Caching.class)
  @InsertProvider(type = WrapperProvider.class, method = WrapperProvider.insertInWrapper)
  int insert(T entity);

  /**
   * 保存实体（字段可为空）
   *
   * @param entity 实体类
   * @return 1成功，0失败
   */
  @Lang(Caching.class)
  @InsertProvider(type = WrapperProvider.class, method = WrapperProvider.insertNullableInWrapper)
  int insertNullable(T entity);

  /**
   * 根据主键更新实体（字段不为空）
   *
   * @param entity 实体类
   * @return 1成功，0失败
   */
  @Lang(Caching.class)
  @UpdateProvider(type = WrapperProvider.class, method = WrapperProvider.updateByIdInWrapper)
  int updateById(T entity);

  /**
   * 根据主键更新实体（字段可为空）
   *
   * @param entity 实体类
   * @return 1成功，0失败
   */
  @Lang(Caching.class)
  @UpdateProvider(type = WrapperProvider.class, method = WrapperProvider.updateNullableByIdInWrapper)
  int updateNullableById(T entity);

  /**
   * 根据主键删除
   *
   * @param id 主键
   * @return 1成功，0失败
   */
  @Lang(Caching.class)
  @DeleteProvider(type = WrapperProvider.class, method = WrapperProvider.deleteByIdInWrapper)
  int deleteById(I id);

  /**
   * 根据主键查询实体
   *
   * @param id 主键
   * @return 实体
   */
  @Lang(Caching.class)
  @SelectProvider(type = WrapperProvider.class, method = WrapperProvider.selectByIdInWrapper)
  Optional<T> selectById(I id);

  /**
   * 根据 Wrapper 条件批量更新实体信息
   *
   * @param entity  实体类
   * @param wrapper 条件
   * @return 大于等于1成功，0失败
   */
  @Lang(Caching.class)
  @UpdateProvider(type = WrapperProvider.class, method = WrapperProvider.updateByWrapper)
  int update(@Param("entity") T entity, @Param("wrapper") Wrapper<T> wrapper);

  /**
   * 根据 Wrapper 删除
   *
   * @param wrapper 条件
   * @return 大于等于1成功，0失败
   */
  @Lang(Caching.class)
  @DeleteProvider(type = WrapperProvider.class, method = WrapperProvider.deleteByWrapper)
  int delete(Wrapper<T> wrapper);

  /**
   * 根据 Wrapper 条件批量查询
   *
   * @param wrapper 条件
   * @return 实体列表
   */
  @Lang(Caching.class)
  @SelectProvider(type = WrapperProvider.class, method = WrapperProvider.selectByWrapper)
  List<T> selectList(Wrapper<T> wrapper);

  /**
   * 根据 Wrapper 条件批量查询
   *
   * @param rowBounds 分页信息
   * @param wrapper   条件
   * @return 实体列表
   */
  List<T> selectList(RowBounds rowBounds, Wrapper<T> wrapper);

  /**
   * 根据 Wrapper 条件查询单个实体
   *
   * @param wrapper @param wrapper 条件
   * @return 单个实体，查询结果由多条时报错
   * @throws TooManyResultsException exception
   */
  @Lang(Caching.class)
  @SelectProvider(type = WrapperProvider.class, method = WrapperProvider.selectByWrapper)
  Optional<T> selectOneThrow(Wrapper<T> wrapper) throws TooManyResultsException;

  /**
   * 根据 Wrapper 条件查询单个实体
   *
   * @param wrapper 条件
   * @return 单个实体
   */
  default Optional<T> selectOne(Wrapper<T> wrapper) {
    return selectList(new RowBounds(0, 1), wrapper).stream().findFirst();
  }

  /**
   * 根据 Wrapper 条件查询总数
   *
   * @param wrapper 条件
   * @return 总数
   */
  @Lang(Caching.class)
  @SelectProvider(type = WrapperProvider.class, method = WrapperProvider.countByWrapper)
  long count(Wrapper<T> wrapper);

}
