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

import io.mybatis.provider.BaseTest;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

public class UserBaseMapperTest extends BaseTest {

  @Test
  public void testSelectById() {
    try (SqlSession sqlSession = getSqlSession()) {
      UserBaseMapper userMapper = sqlSession.getMapper(UserBaseMapper.class);

      User user = userMapper.selectByPrimaryKey(1L).get();
      Assert.assertNotNull(user);
      Assert.assertEquals("张无忌", user.getUsername());
      Assert.assertNull(user.getSex());

      user.setId(999L);
      Assert.assertEquals(1, userMapper.insert(user));

      int count = userMapper.deleteByPrimaryKey(user.getId());
      Assert.assertEquals(1, count);
      count = userMapper.deleteByPrimaryKey(user.getId());
      Assert.assertEquals(0, count);
      sqlSession.rollback();
    }
  }

}
