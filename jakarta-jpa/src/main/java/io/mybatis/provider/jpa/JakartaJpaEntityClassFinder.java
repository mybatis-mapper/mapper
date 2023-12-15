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

package io.mybatis.provider.jpa;

import io.mybatis.provider.defaults.GenericEntityClassFinder;
import jakarta.persistence.Table;

/**
 * 支持识别带有 @javax.persistence.Table 的实体类或者不带任何注解的POJO
 *
 * @author liuzh
 */
public class JakartaJpaEntityClassFinder extends GenericEntityClassFinder {

  @Override
  public boolean isEntityClass(Class<?> clazz) {
    //带注解或不是简单类型和枚举的都算实体
    return clazz.isAnnotationPresent(Table.class)
        || (!clazz.isPrimitive()
        && !clazz.isInterface()
        && !clazz.isArray()
        && !clazz.isAnnotation()
        && !clazz.isEnum() && !SimpleTypeUtil.isSimpleType(clazz));
  }

  @Override
  public int getOrder() {
    return super.getOrder() + 100;
  }
}
