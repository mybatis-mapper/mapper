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

package io.mybatis.mapper.fn;

import io.mybatis.mapper.model.User;
import io.mybatis.mapper.model.UserIs;
import org.junit.Assert;
import org.junit.Test;

public class FnTest {

  @Test
  public void test() {
    Assert.assertEquals("sex", ((Fn<User, Object>) User::getSex).toField());
    Assert.assertEquals("sex", ((Fn<User, Object>) User::getSex).toColumn());
    Assert.assertEquals("userName", ((Fn<User, Object>) User::getUserName).toField());
    Assert.assertEquals("name", ((Fn<User, Object>) User::getUserName).toColumn());
    Assert.assertEquals("admin", ((Fn<UserIs, Object>) UserIs::isAdmin).toField());
    Assert.assertEquals("is_admin", ((Fn<UserIs, Object>) UserIs::isAdmin).toColumn());
  }

}
