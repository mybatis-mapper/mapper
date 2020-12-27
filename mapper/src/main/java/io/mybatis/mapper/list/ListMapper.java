/*
 * Copyright 2020 the original author or authors.
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

package io.mybatis.mapper.list;

import io.mybatis.provider.Caching;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 批量操作方法
 *
 * @param <T> 实体类类型
 * @author liuzh
 */
public interface ListMapper<T> {

  /**
   * 批量保存实体，需要数据库支持批量插入的语法
   *
   * @param entityList 实体列表
   * @return 结果数等于 entityList.size() 时成功，不相等时失败
   */
  @Lang(Caching.class)
  @InsertProvider(type = ListProvider.class, method = "insertList")
  int insertList(@Param("entityList") List<? extends T> entityList);

}
