package io.mybatis.mapper.logical;

import io.mybatis.mapper.BaseMapper;
import io.mybatis.mapper.example.Example;
import io.mybatis.mapper.fn.Fn;
import io.mybatis.mapper.fn.FnMapper;
import io.mybatis.provider.Caching;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.RowBounds;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * 逻辑删除的 Mapper 方法
 * <p>继承此mapper会覆盖BaseMapper、FnMapper中的查、删、改相关方法</p>
 *
 * @author hzw
 */
public interface LogicalMapper<T, I extends Serializable> extends BaseMapper<T, I>, FnMapper<T> {
  /* BaseMapper +++ */

  @Override
  @Lang(Caching.class)
  @UpdateProvider(type = LogicalProvider.class, method = "updateByPrimaryKeySelectiveWithForceFields")
  <S extends T> int updateByPrimaryKeySelectiveWithForceFields(@Param("entity") S entity, @Param("fns") Fn.Fns<T> forceUpdateFields);

  /* BaseMapper --- */

  /* FnMapper +++ */

  @Override
  @Lang(Caching.class)
  @SelectProvider(type = LogicalProvider.class, method = "selectColumns")
  Optional<T> selectColumnsOne(@Param("entity") T entity, @Param("fns") Fn.Fns<T> selectFields);

  @Override
  @Lang(Caching.class)
  @SelectProvider(type = LogicalProvider.class, method = "selectColumns")
  List<T> selectColumns(@Param("entity") T entity, @Param("fns") Fn.Fns<T> selectFields);

  /* FnMapper --- */

  /* EntityMapper +++ */

  @Override
  @Lang(Caching.class)
  @DeleteProvider(type = LogicalProvider.class, method = "deleteByPrimaryKey")
  int deleteByPrimaryKey(I id);

  @Override
  @Lang(Caching.class)
  @DeleteProvider(type = LogicalProvider.class, method = "delete")
  int delete(T entity);

  @Override
  @Lang(Caching.class)
  @UpdateProvider(type = LogicalProvider.class, method = "updateByPrimaryKey")
  <S extends T> int updateByPrimaryKey(S entity);

  @Override
  @Lang(Caching.class)
  @UpdateProvider(type = LogicalProvider.class, method = "updateByPrimaryKeySelective")
  <S extends T> int updateByPrimaryKeySelective(S entity);

  @Override
  @Lang(Caching.class)
  @SelectProvider(type = LogicalProvider.class, method = "selectByPrimaryKey")
  Optional<T> selectByPrimaryKey(I id);

  @Override
  @Lang(Caching.class)
  @SelectProvider(type = LogicalProvider.class, method = "select")
  Optional<T> selectOne(T entity);

  @Override
  @Lang(Caching.class)
  @SelectProvider(type = LogicalProvider.class, method = "select")
  List<T> selectList(T entity);

  @Override
  @Lang(Caching.class)
  @SelectProvider(type = LogicalProvider.class, method = "selectCount")
  long selectCount(T entity);

  /* EntityMapper --- */

  /* CursorMapper +++ */

  @Override
  @Lang(Caching.class)
  @SelectProvider(type = LogicalProvider.class, method = "select")
  Cursor<T> selectCursor(T entity);

  @Override
  @Lang(Caching.class)
  @SelectProvider(type = LogicalProvider.class, method = "selectByExample")
  Cursor<T> selectCursorByExample(Example<T> example);

  /* CursorMapper --- */

  /* ExampleMapper +++ */
  @Override
  default Example<T> example() {
    return BaseMapper.super.example();
  }

  @Override
  @Lang(Caching.class)
  @DeleteProvider(type = LogicalProvider.class, method = "deleteByExample")
  int deleteByExample(Example<T> example);

  @Override
  @Lang(Caching.class)
  @UpdateProvider(type = LogicalProvider.class, method = "updateByExample")
  <S extends T> int updateByExample(@Param("entity") S entity, @Param("example") Example<T> example);

  @Override
  @Lang(Caching.class)
  @UpdateProvider(type = LogicalProvider.class, method = "updateByExampleSetValues")
  int updateByExampleSetValues(@Param("example") Example<T> example);

  @Override
  @Lang(Caching.class)
  @UpdateProvider(type = LogicalProvider.class, method = "updateByExampleSelective")
  <S extends T> int updateByExampleSelective(@Param("entity") S entity, @Param("example") Example<T> example);

  @Override
  @Lang(Caching.class)
  @SelectProvider(type = LogicalProvider.class, method = "selectByExample")
  List<T> selectByExample(Example<T> example);

  @Override
  @Lang(Caching.class)
  @SelectProvider(type = LogicalProvider.class, method = "selectByExample")
  Optional<T> selectOneByExample(Example<T> example);

  @Override
  @Lang(Caching.class)
  @SelectProvider(type = LogicalProvider.class, method = "countByExample")
  long countByExample(Example<T> example);

  @Override
  List<T> selectByExample(Example<T> example, RowBounds rowBounds);

  /* ExampleMapper --- */
}
