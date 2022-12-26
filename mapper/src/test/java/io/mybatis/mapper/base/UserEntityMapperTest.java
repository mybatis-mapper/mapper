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
import io.mybatis.mapper.SumMapper;
import io.mybatis.mapper.UserMapper;
import io.mybatis.mapper.model.User;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

public class UserEntityMapperTest extends BaseMapperTest {

  @Test
  public void testInsert() {
    SqlSession sqlSession = getSqlSession();
    try {
      EntityMapper<User, Long> entityMapper = sqlSession.getMapper(UserMapper.class);
      User user = new User();
      user.setUserName("测试");
      entityMapper.insert(user);
      Assert.assertNotNull(user.getId());
      entityMapper.selectByPrimaryKey(user.getId());

      user = new User();
      user.setUserName("测试");
      entityMapper.insert(user);
      Assert.assertNotNull(user.getId());
      entityMapper.selectByPrimaryKey(user.getId());
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
      EntityMapper<User, Long> entityMapper = sqlSession.getMapper(UserMapper.class);
      User user = new User();
      user.setSex("性别");
      entityMapper.insertSelective(user);
      Assert.assertNotNull(user.getId());
      Optional<User> userOptional = entityMapper.selectByPrimaryKey(user.getId());
      Assert.assertTrue(userOptional.isPresent());
      Assert.assertEquals("DEFAULT", userOptional.get().getUserName());
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
      EntityMapper<User, Long> entityMapper = sqlSession.getMapper(UserMapper.class);
      Assert.assertEquals(1, entityMapper.deleteByPrimaryKey(1L));
      Assert.assertTrue(!entityMapper.selectByPrimaryKey(1L).isPresent());
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
      EntityMapper<User, Long> entityMapper = sqlSession.getMapper(UserMapper.class);
      User user = new User();
      user.setId(1L);
      Assert.assertEquals(1, entityMapper.delete(user));
      Assert.assertTrue(!entityMapper.selectByPrimaryKey(1L).isPresent());
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
      EntityMapper<User, Long> entityMapper = sqlSession.getMapper(UserMapper.class);
      User user = new User();
      user.setId(1L);
      user.setUserName("男主角");
      Assert.assertEquals(1, entityMapper.updateByPrimaryKey(user));
      user = entityMapper.selectByPrimaryKey(1L).get();
      Assert.assertEquals("男主角", user.getUserName());
      Assert.assertNull(user.getSex());
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
      EntityMapper<User, Long> entityMapper = sqlSession.getMapper(UserMapper.class);
      User user = new User();
      user.setId(1L);
      user.setUserName("男主角");
      Assert.assertEquals(1, entityMapper.updateByPrimaryKeySelective(user));
      user = entityMapper.selectByPrimaryKey(1L).get();
      Assert.assertEquals("男主角", user.getUserName());
      Assert.assertEquals("男", user.getSex());
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
      EntityMapper<User, Long> entityMapper = sqlSession.getMapper(UserMapper.class);
      Optional<User> user = entityMapper.selectByPrimaryKey(1L);
      Assert.assertTrue(user.isPresent());
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testSelectOne() {
    SqlSession sqlSession = getSqlSession();
    try {
      EntityMapper<User, Long> entityMapper = sqlSession.getMapper(UserMapper.class);
      User user = new User();
      user.setId(1L);
      Optional<User> optionalCountry = entityMapper.selectOne(user);
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
      EntityMapper<User, Long> entityMapper = sqlSession.getMapper(UserMapper.class);
      User user = new User();
      List<User> countries = entityMapper.selectList(user);
      Assert.assertEquals(53, countries.size());
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testSelectCount() {
    SqlSession sqlSession = getSqlSession();
    try {
      EntityMapper<User, Long> entityMapper = sqlSession.getMapper(UserMapper.class);
      User user = new User();
      user.setSex("女");
      long count = entityMapper.selectCount(user);
      Assert.assertEquals(16, count);
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testSum() {
    SqlSession sqlSession = getSqlSession();
    try {
      SumMapper<User> userMapper = sqlSession.getMapper(UserMapper.class);
      User user = new User();
      user.setSex("女");
      long sum = userMapper.sum(User::getId, user);
      Assert.assertEquals(410, sum);
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testEntityInfo() {
    SqlSession sqlSession = getSqlSession();
    try {
      EntityMapper<User, Long> entityMapper = sqlSession.getMapper(UserMapper.class);
      Assert.assertEquals(User.class, entityMapper.entityClass());
      Assert.assertEquals("user", entityMapper.entityTable().table());
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test(expected = PersistenceException.class)
  public void testUnsupported() {
    SqlSession sqlSession = getSqlSession();
    try {
      UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
      userMapper.unsupported();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }
}


