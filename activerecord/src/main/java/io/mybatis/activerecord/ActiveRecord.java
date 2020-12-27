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

package io.mybatis.activerecord;

import java.io.Serializable;

/**
 * 建议将继承该抽象类的实现类的作用范围控制在 Service 层，不能超出范围，其它层使用时转换为 VO 或 DTO 后使用
 *
 * @param <T> 实体类类型
 * @param <I> 主键类型
 * @author liuzh
 */
public interface ActiveRecord<T, I extends Serializable>
    extends EntityRecord<T, I>, ExampleRecord<T, I> {

}
