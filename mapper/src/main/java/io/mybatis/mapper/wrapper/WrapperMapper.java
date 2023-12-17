package io.mybatis.mapper.wrapper;

import io.mybatis.provider.Caching;
import io.mybatis.provider.EntityInfoMapper;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.apache.ibatis.session.RowBounds;

import java.util.List;
import java.util.Optional;

/**
 * Wrapper 相关方法
 *
 * @param <T> 实体类
 * @param <E> 符合Wrapper数据结构的对象，也可以是 MBG 生成 XXXWrapper 对象。
 * @author liuzh
 */
public interface WrapperMapper<T, E> extends EntityInfoMapper<T> {

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
   * 保存实体（字段不为空）
   *
   * @param entity 实体类
   * @return 1成功，0失败
   */
  @Lang(Caching.class)
  @InsertProvider(type = WrapperProvider.class, method = WrapperProvider.insertInWrapper)
  int insertInWrapper(T entity);

  /**
   * 保存实体（字段可为空）
   *
   * @param entity 实体类
   * @return 1成功，0失败
   */
  @Lang(Caching.class)
  @InsertProvider(type = WrapperProvider.class, method = WrapperProvider.insertNullableInWrapper)
  int insertNullableInWrapper(T entity);

  /**
   * 根据主键更新实体（字段不为空）
   *
   * @param entity 实体类
   * @return 1成功，0失败
   */
  @Lang(Caching.class)
  @UpdateProvider(type = WrapperProvider.class, method = WrapperProvider.updateByIdInWrapper)
  int updateByIdInWrapper(T entity);

  /**
   * 根据主键更新实体（字段可为空）
   *
   * @param entity 实体类
   * @return 1成功，0失败
   */
  @Lang(Caching.class)
  @UpdateProvider(type = WrapperProvider.class, method = WrapperProvider.updateNullableByIdInWrapper)
  int updateNullableByIdInWrapper(T entity);

  /**
   * 根据主键删除
   *
   * @param id 主键
   * @return 1成功，0失败
   */
  @Lang(Caching.class)
  @DeleteProvider(type = WrapperProvider.class, method = WrapperProvider.deleteByIdInWrapper)
  int deleteByIdInWrapper(Object id);

  /**
   * 根据主键查询实体
   *
   * @param id 主键
   * @return 实体
   */
  @Lang(Caching.class)
  @SelectProvider(type = WrapperProvider.class, method = WrapperProvider.selectByIdInWrapper)
  Optional<T> selectByIdInWrapper(Object id);

  /**
   * 根据 Wrapper 条件批量更新实体信息
   *
   * @param entity  实体类
   * @param wrapper 条件
   * @return 大于等于1成功，0失败
   */
  @Lang(Caching.class)
  @UpdateProvider(type = WrapperProvider.class, method = WrapperProvider.updateByWrapper)
  int updateByWrapper(@Param("entity") T entity, @Param("wrapper") E wrapper);

  /**
   * 根据 Wrapper 删除
   *
   * @param wrapper 条件
   * @return 大于等于1成功，0失败
   */
  @Lang(Caching.class)
  @DeleteProvider(type = WrapperProvider.class, method = WrapperProvider.deleteByWrapper)
  int deleteByWrapper(E wrapper);

  /**
   * 根据 Wrapper 条件批量查询
   *
   * @param wrapper 条件
   * @return 实体列表
   */
  @Lang(Caching.class)
  @SelectProvider(type = WrapperProvider.class, method = WrapperProvider.selectByWrapper)
  List<T> selectListByWrapper(E wrapper);

  /**
   * 根据 Wrapper 条件批量查询
   *
   * @param rowBounds 分页信息
   * @param wrapper   条件
   * @return 实体列表
   */
  List<T> selectListByWrapper(RowBounds rowBounds, E wrapper);

  /**
   * 根据 Wrapper 条件查询单个实体
   *
   * @param wrapper @param wrapper 条件
   * @return 单个实体，查询结果由多条时报错
   * @throws TooManyResultsException exception
   */
  @Lang(Caching.class)
  @SelectProvider(type = WrapperProvider.class, method = WrapperProvider.selectByWrapper)
  Optional<T> selectOneThrowByWrapper(E wrapper) throws TooManyResultsException;

  /**
   * 根据 Wrapper 条件查询单个实体
   *
   * @param wrapper 条件
   * @return 单个实体
   */
  default Optional<T> selectOneByWrapper(E wrapper) {
    return selectListByWrapper(new RowBounds(0, 1), wrapper).stream().findFirst();
  }

  /**
   * 根据 Wrapper 条件查询总数
   *
   * @param wrapper 条件
   * @return 总数
   */
  @Lang(Caching.class)
  @SelectProvider(type = WrapperProvider.class, method = WrapperProvider.countByWrapper)
  long countByWrapper(E wrapper);
}
