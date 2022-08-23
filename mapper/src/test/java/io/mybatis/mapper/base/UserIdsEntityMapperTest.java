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

package io.mybatis.mapper.base;

import io.mybatis.mapper.BaseMapperTest;
import io.mybatis.mapper.UserIdsMapper;
import io.mybatis.mapper.model.UserIds;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

public class UserIdsEntityMapperTest extends BaseMapperTest {

  @Test
  public void testInsert() {
    SqlSession sqlSession = getSqlSession();
    try {
      EntityMapper<UserIds, UserIds> entityMapper = sqlSession.getMapper(UserIdsMapper.class);
      UserIds user = new UserIds();
      user.setId1(100L);
      user.setId2(100L);
      user.setName("测试");
      Assert.assertEquals(1, entityMapper.insert(user));
      Optional<UserIds> userIdsOptional = entityMapper.selectByPrimaryKey(new UserIds(100L, 100L));
      Assert.assertTrue(userIdsOptional.isPresent());
      Assert.assertEquals("测试", userIdsOptional.get().getName());
      Assert.assertFalse(entityMapper.selectByPrimaryKey(new UserIds(100L, 1L)).isPresent());
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testInsertSelective() {
    SqlSession sqlSession = getSqlSession();
    try {
      EntityMapper<UserIds, UserIds> entityMapper = sqlSession.getMapper(UserIdsMapper.class);
      UserIds user = new UserIds();
      user.setId1(100L);
      user.setId2(100L);
      Assert.assertEquals(1, entityMapper.insertSelective(user));
      Optional<UserIds> userIdsOptional = entityMapper.selectByPrimaryKey(new UserIds(100L, 100L));
      Assert.assertTrue(userIdsOptional.isPresent());
      Assert.assertEquals("DEFAULT", userIdsOptional.get().getName());
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testDeleteByPrimaryKey() {
    SqlSession sqlSession = getSqlSession();
    try {
      EntityMapper<UserIds, UserIds> entityMapper = sqlSession.getMapper(UserIdsMapper.class);
      Assert.assertEquals(0, entityMapper.deleteByPrimaryKey(new UserIds(1L, null)));
      Assert.assertEquals(0, entityMapper.deleteByPrimaryKey(new UserIds(1L, 0L)));
      Assert.assertEquals(1, entityMapper.deleteByPrimaryKey(new UserIds(1L, 1L)));
      Assert.assertFalse(entityMapper.selectByPrimaryKey(new UserIds(1L, 1L)).isPresent());
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testDelete() {
    SqlSession sqlSession = getSqlSession();
    try {
      EntityMapper<UserIds, UserIds> entityMapper = sqlSession.getMapper(UserIdsMapper.class);
      UserIds user = new UserIds();
      user.setId1(1L);
      user.setId2(1L);
      Assert.assertEquals(1, entityMapper.delete(user));
      user.setId2(null);
      Assert.assertEquals(3, entityMapper.delete(user));
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testUpdateByPrimaryKey() {
    SqlSession sqlSession = getSqlSession();
    try {
      EntityMapper<UserIds, UserIds> entityMapper = sqlSession.getMapper(UserIdsMapper.class);
      UserIds user = new UserIds();
      user.setId1(1L);
      user.setId2(1L);
      user.setName("男主角");
      Assert.assertEquals(1, entityMapper.updateByPrimaryKey(user));
      user = entityMapper.selectByPrimaryKey(new UserIds(1L, 1L)).get();
      Assert.assertEquals("男主角", user.getName());
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testUpdateByPrimaryKeySelective() {
    SqlSession sqlSession = getSqlSession();
    try {
      EntityMapper<UserIds, UserIds> entityMapper = sqlSession.getMapper(UserIdsMapper.class);
      UserIds user = new UserIds();
      user.setId1(1L);
      user.setId2(1L);
      Assert.assertEquals(1, entityMapper.updateByPrimaryKeySelective(user));
      user = entityMapper.selectByPrimaryKey(new UserIds(1L, 1L)).get();
      Assert.assertEquals("张无忌1", user.getName());
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testSelectByPrimaryKey() {
    SqlSession sqlSession = getSqlSession();
    try {
      EntityMapper<UserIds, UserIds> entityMapper = sqlSession.getMapper(UserIdsMapper.class);
      Assert.assertTrue(entityMapper.selectByPrimaryKey(new UserIds(1L, 1L)).isPresent());
      Assert.assertFalse(entityMapper.selectByPrimaryKey(new UserIds(1L, null)).isPresent());
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testSelectOne() {
    SqlSession sqlSession = getSqlSession();
    try {
      EntityMapper<UserIds, UserIds> entityMapper = sqlSession.getMapper(UserIdsMapper.class);
      UserIds user = new UserIds();
      user.setId2(1L);
      Optional<UserIds> optionalCountry = entityMapper.selectOne(user);
      System.out.println(optionalCountry.get());
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testSelectList() {
    SqlSession sqlSession = getSqlSession();
    try {
      EntityMapper<UserIds, UserIds> entityMapper = sqlSession.getMapper(UserIdsMapper.class);
      UserIds user = new UserIds();
      user.setId1(1L);
      List<UserIds> users = entityMapper.selectList(user);
      Assert.assertEquals(4, users.size());
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }
}


