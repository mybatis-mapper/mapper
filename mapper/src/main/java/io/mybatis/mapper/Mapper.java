/*
 * Copyright 2020-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mybatis.mapper;

import io.mybatis.mapper.base.EntityProvider;
import io.mybatis.provider.Caching;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Options;

import java.io.Serializable;

/**
 * 自定义 Mapper 示例，这个 Mapper 基于主键自增重写了 insert 方法，主要用作示例
 * <p>
 * 当你使用 Oracle 或其他数据库时，insert 重写时也可以使用 @SelectKey 注解对主键进行定制
 *
 * @param <T> 实体类类型
 * @param <I> 主键类型
 * @author liuzh
 */
public interface Mapper<T, I extends Serializable> extends BaseMapper<T, I> {

  /**
   * 保存实体，默认主键自增，并且名称为 id
   * <p>
   * 这个方法是个示例，你可以在自己的接口中使用相同的方式覆盖父接口中的配置
   *
   * @param entity 实体类
   * @return 1成功，0失败
   */
  @Override
  @Lang(Caching.class)
  //@SelectKey(statement = "SELECT SEQ.NEXTVAL FROM DUAL", keyProperty = "id", before = true, resultType = long.class)
  @Options(useGeneratedKeys = true, keyProperty = "id")
  @InsertProvider(type = EntityProvider.class, method = "insert")
  int insert(T entity);

  /**
   * 保存实体中不为空的字段，默认主键自增，并且名称为 id
   * <p>
   * 这个方法是个示例，你可以在自己的接口中使用相同的方式覆盖父接口中的配置
   *
   * @param entity 实体类
   * @return 1成功，0失败
   */
  @Override
  @Lang(Caching.class)
  //@SelectKey(statement = "SELECT SEQ.NEXTVAL FROM DUAL", keyProperty = "id", before = true, resultType = long.class)
  @Options(useGeneratedKeys = true, keyProperty = "id")
  @InsertProvider(type = EntityProvider.class, method = "insertSelective")
  int insertSelective(T entity);

}
