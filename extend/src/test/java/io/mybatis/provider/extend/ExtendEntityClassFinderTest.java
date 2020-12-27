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

package io.mybatis.provider.extend;

import io.mybatis.provider.extend.mapper.UserMapper;
import io.mybatis.provider.extend.model.User;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public class ExtendEntityClassFinderTest {

  @Test
  public void testFindEntityClass() {
    ExtendEntityClassFinder classFinder = new ExtendEntityClassFinder();
    Optional<Class<?>> optionalClass = classFinder.findEntityClass(UserMapper.class, null);
    Assert.assertTrue(optionalClass.isPresent());
    Assert.assertEquals(User.class, optionalClass.get());
  }

}
