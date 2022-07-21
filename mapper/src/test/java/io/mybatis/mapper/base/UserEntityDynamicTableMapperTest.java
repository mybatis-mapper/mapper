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

public class UserEntityDynamicTableMapperTest extends BaseMapperTest {

  @Test
  public void testInsert() {
    SqlSession sqlSession = getSqlSession();
    try {
      EntityMapper<User, Long> entityMapper = sqlSession.getMapper(UserMapper.class);
      User user = new User();
      user.setUserName("测试");
      user.setSuffix("_test");
      entityMapper.insert(user);
      Assert.assertNotNull(user.getId());
      User qo = new User();
      qo.setId(user.getId());
      qo.setSuffix("_test");
      entityMapper.selectOne(qo);
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
      user.setSuffix("_test");
      entityMapper.insertSelective(user);
      Assert.assertNotNull(user.getId());
      User qo = new User();
      qo.setId(user.getId());
      qo.setSuffix("_test");
      Optional<User> userOptional = entityMapper.selectOne(qo);
      Assert.assertTrue(userOptional.isPresent());
      Assert.assertEquals("DEFAULT", userOptional.get().getUserName());
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
      user.setSuffix("_test");
      Assert.assertEquals(1, entityMapper.delete(user));
      User qo = new User();
      qo.setId(user.getId());
      qo.setSuffix("_test");
      Optional<User> userOptional = entityMapper.selectOne(qo);
      Assert.assertTrue(!userOptional.isPresent());
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
      user.setSuffix("_test");
      Assert.assertEquals(1, entityMapper.updateByPrimaryKey(user));
      User qo = new User();
      qo.setId(user.getId());
      qo.setSuffix("_test");
      user = entityMapper.selectOne(qo).get();
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
      user.setSuffix("_test");
      Assert.assertEquals(1, entityMapper.updateByPrimaryKeySelective(user));
      User qo = new User();
      qo.setId(user.getId());
      qo.setSuffix("_test");
      user = entityMapper.selectOne(qo).get();
      Assert.assertEquals("男主角", user.getUserName());
      Assert.assertEquals("男", user.getSex());
      sqlSession.rollback();
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
      user.setSuffix("_test");
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
      user.setSuffix("_test");
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
      user.setSuffix("_test");
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

}


