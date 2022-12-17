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

package io.mybatis.mapper.fn;

import io.mybatis.mapper.BaseMapperTest;
import io.mybatis.mapper.UserIdsMapper;
import io.mybatis.mapper.model.UserIds;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

public class UserIdsFnMapperTest extends BaseMapperTest {

  @Test
  public void testUpdateByPrimaryKeySelectiveWithForceFields() {
    SqlSession sqlSession = getSqlSession();
    try {
      UserIdsMapper mapper = sqlSession.getMapper(UserIdsMapper.class);
      UserIds id = new UserIds(1L, 1L);
      UserIds user = mapper.selectByPrimaryKey(id).get();
      user.setName(null);
      int count = mapper.updateByPrimaryKeySelectiveWithForceFields(user, Fn.of(UserIds::getName));
      Assert.assertEquals(1, count);
      user = mapper.selectByPrimaryKey(id).get();
      Assert.assertNull(user.getName());
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testSelectColumnsOne() {
    SqlSession sqlSession = getSqlSession();
    try {
      UserIdsMapper mapper = sqlSession.getMapper(UserIdsMapper.class);
      UserIds user = new UserIds(1L, 1L);
      Optional<UserIds> optionalUser = mapper.selectColumnsOne(user, Fn.of(UserIds::getName));
      Assert.assertTrue(optionalUser.isPresent());
      Assert.assertNull(optionalUser.get().getId1());
      Assert.assertNull(optionalUser.get().getId2());
      Assert.assertNotNull(optionalUser.get().getName());
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testSelectColumns() {
    SqlSession sqlSession = getSqlSession();
    try {
      UserIdsMapper mapper = sqlSession.getMapper(UserIdsMapper.class);
      UserIds user = new UserIds();
      user.setId1(1L);
      List<UserIds> users = mapper.selectColumns(user, Fn.of(UserIds::getName));
      users.forEach(u -> {
        Assert.assertNull(u.getId1());
        Assert.assertNull(u.getId2());
        Assert.assertNotNull(u.getName());
      });

      users = mapper.selectColumns(user, Fn.of(UserIds::getId1, UserIds::getName));
      users.forEach(u -> {
        Assert.assertNotNull(u.getId1());
        Assert.assertNull(u.getId2());
        Assert.assertNotNull(u.getName());
      });

      users = mapper.selectColumns(user, Fn.of(UserIds.class, "id1", "name"));
      users.forEach(u -> {
        Assert.assertNotNull(u.getId1());
        Assert.assertNull(u.getId2());
        Assert.assertNotNull(u.getName());
      });
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

}


