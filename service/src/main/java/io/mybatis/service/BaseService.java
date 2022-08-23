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

package io.mybatis.service;

import java.io.Serializable;

/**
 * 基础接口，包含实体类基本接口和 Example 接口
 * <p>
 * 自己的接口不一定要实现这个接口，直接继承会暴露过多的接口，
 * 可以直接在实现类中继承 AbstractService 实现，对外暴露的接口在自己接口中定义，
 * 自己定义的接口和 AbstractService 实现方法的定义一样时，不需要提供实现方法
 *
 * @param <T> 实体类类型
 * @param <I> 主键类型
 * @author liuzh
 */
public interface BaseService<T, I extends Serializable> extends EntityService<T, I>, ExampleService<T, I> {

}
