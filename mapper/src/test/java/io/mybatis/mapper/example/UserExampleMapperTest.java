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

package io.mybatis.mapper.example;

import io.mybatis.common.util.Utils;
import io.mybatis.mapper.BaseMapperTest;
import io.mybatis.mapper.UserMapper;
import io.mybatis.mapper.model.User;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class UserExampleMapperTest extends BaseMapperTest {

  @Test
  public void testExample() {
    SqlSession sqlSession = getSqlSession();
    try {
      ExampleMapper<User, Example<User>> exampleMapper = sqlSession.getMapper(UserMapper.class);
      Example<User> example = exampleMapper.example();
      example.selectColumns(User::getUserName, User::getSex);
      example.createCriteria().andLike(User::getUserName, "殷%");
      Example.Criteria<User> criteria = example.createCriteria();
      criteria.andLike(User::getUserName, "张%");
      example.or(criteria);
      List<User> users = exampleMapper.selectByExample(example);
      Assert.assertEquals(10, users.size());
      Assert.assertEquals("张无忌", users.get(0).getUserName());

      example.clear();
      example.setSelectColumns("count(distinct sex) as id");
      Optional<User> user = exampleMapper.selectOneByExample(example);
      Assert.assertTrue(user.isPresent());
      Assert.assertEquals(2L, user.get().getId().longValue());

      example.clear();
      example.setStartSql("select * from (");
      example.setEndSql(") tmp limit 1");
      //设置查询id
      example.selectColumns(User::getId);
      //设置排除id，会覆盖前面的配置
      example.excludeColumns(User::getId);
      example.createCriteria().andEqualTo(User::getSex, "女");
      user = exampleMapper.selectOneByExample(example);
      Assert.assertTrue(user.isPresent());
      Assert.assertEquals("赵敏", user.get().getUserName());

      example.clear();
      example.orderBy(User::getId, Example.Order.DESC);
      example.orderBy(User::getSex, Example.Order.ASC);
      example.setEndSql("limit 1");
      user = exampleMapper.selectOneByExample(example);
      Assert.assertTrue(user.isPresent());
      Assert.assertEquals("韩千叶", user.get().getUserName());

      example.clear();
      example.orderByDesc(User::getId, User::getSex);
      example.setEndSql("limit 1");
      user = exampleMapper.selectOneByExample(example);
      Assert.assertTrue(user.isPresent());
      Assert.assertEquals("韩千叶", user.get().getUserName());

      example.clear();
      example.setDistinct(true);
      example.selectColumns(User::getSex);
      long count = exampleMapper.countByExample(example);
      Assert.assertEquals(2, count);

      example.clear();
      example.createCriteria().andLike(User::getUserName, "殷%").andCondition("length(name) < 3");
      example.or().andNotLike(User::getUserName, "张%").andCondition("length(name) > ", 3);
      List<User> users2 = exampleMapper.selectByExample(example);
      Assert.assertEquals(4, users2.size());

      example.clear();
      example.createCriteria().andBetween(User::getId, 1, 10)
          .andNotBetween(User::getId, 3, 8);
      Assert.assertEquals(4, exampleMapper.countByExample(example));

      example.clear();
      example.createCriteria().andIsNull(User::getId);
      Assert.assertEquals(0, exampleMapper.countByExample(example));

      example.clear();
      example.createCriteria().andIsNotNull(User::getId);
      Assert.assertEquals(53, exampleMapper.countByExample(example));

      example.clear();
      example.createCriteria().andNotEqualTo(User::getId, 10)
          .andGreaterThan(User::getId, 3).andGreaterThanOrEqualTo(User::getId, 5)
          .andLessThan(User::getId, 15).andLessThanOrEqualTo(User::getId, 14);
      Assert.assertEquals(9, exampleMapper.countByExample(example));

      example.clear();
      example.createCriteria().andIn(User::getId, Arrays.asList(1, 2, 3, 4, 5))
          .andNotIn(User::getId, Arrays.asList(2, 3, 4));
      Assert.assertEquals(2, exampleMapper.countByExample(example));
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testDeleteByExample() {
    SqlSession sqlSession = getSqlSession();
    try {
      ExampleMapper<User, Example<User>> exampleMapper = sqlSession.getMapper(UserMapper.class);
      Example<User> example = new Example();
      example.createCriteria().andGreaterThan(User::getId, 10L).andLike(User::getUserName, "殷%");
      Assert.assertEquals(3, exampleMapper.deleteByExample(example));
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test(expected = PersistenceException.class)
  public void testDeleteByExampleEmpty() {
    SqlSession sqlSession = getSqlSession();
    try {
      ExampleMapper<User, Example<User>> exampleMapper = sqlSession.getMapper(UserMapper.class);
      exampleMapper.deleteByExample(new Example<>());
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testUpdateByExample() {
    SqlSession sqlSession = getSqlSession();
    try {
      ExampleMapper<User, Example<User>> exampleMapper = sqlSession.getMapper(UserMapper.class);
      Example<User> example = new Example();
      example.createCriteria().andEqualTo(User::getId, 1L);
      User user = new User();
      user.setId(1L);
      user.setUserName("男主角");
      Assert.assertEquals(1, exampleMapper.updateByExample(user, example));
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testUpdateByExampleSetValues() {
    SqlSession sqlSession = getSqlSession();
    try {
      ExampleMapper<User, Example<User>> exampleMapper = sqlSession.getMapper(UserMapper.class);
      Example<User> example = new Example();
      example.createCriteria().andEqualTo(User::getId, 1L);
      example.set(User::getUserName, "男主角").set(User::getSex, "M");

      Assert.assertEquals(1, exampleMapper.updateByExampleSetValues(example));
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test(expected = PersistenceException.class)
  public void testUpdateByExampleEmpty() {
    SqlSession sqlSession = getSqlSession();
    try {
      ExampleMapper<User, Example<User>> exampleMapper = sqlSession.getMapper(UserMapper.class);
      User user = new User();
      user.setId(1L);
      user.setUserName("男主角");
      exampleMapper.updateByExample(user, new Example<>());
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testUpdateByExampleSelective() {
    SqlSession sqlSession = getSqlSession();
    try {
      ExampleMapper<User, Example<User>> exampleMapper = sqlSession.getMapper(UserMapper.class);
      Example<User> example = new Example();
      example.createCriteria().andLessThanOrEqualTo(User::getId, 10L);
      User user = new User();
      user.setUserName("主角");
      Assert.assertEquals(10, exampleMapper.updateByExampleSelective(user, example));
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test(expected = PersistenceException.class)
  public void testUpdateByExampleSelectiveEmpty() {
    SqlSession sqlSession = getSqlSession();
    try {
      ExampleMapper<User, Example<User>> exampleMapper = sqlSession.getMapper(UserMapper.class);
      User user = new User();
      user.setUserName("主角");
      exampleMapper.updateByExampleSelective(user, new Example<>());
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testSelectByExample() {
    SqlSession sqlSession = getSqlSession();
    try {
      ExampleMapper<User, Example<User>> exampleMapper = sqlSession.getMapper(UserMapper.class);
      Example<User> example = new Example();
      example.selectColumns(User::getUserName, User::getSex);
      example.createCriteria().andLike(User::getUserName, "殷%");
      example.setOrderByClause("id desc");
      List<User> users = exampleMapper.selectByExample(example);
      Assert.assertEquals(5, users.size());
      Assert.assertEquals("殷野王", users.get(0).getUserName());
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test(expected = TooManyResultsException.class)
  public void testSelectOneByExample() {
    SqlSession sqlSession = getSqlSession();
    try {
      ExampleMapper<User, Example<User>> exampleMapper = sqlSession.getMapper(UserMapper.class);
      Example<User> example = new Example();
      example.createCriteria().andLike(User::getUserName, "殷野%");
      Optional<User> optionalUser = exampleMapper.selectOneByExample(example);
      Assert.assertTrue(optionalUser.isPresent());
      example = new Example();
      example.createCriteria().andLike(User::getUserName, "殷%");
      exampleMapper.selectOneByExample(example);
      Assert.fail();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testCountByExample() {
    SqlSession sqlSession = getSqlSession();
    try {
      ExampleMapper<User, Example<User>> exampleMapper = sqlSession.getMapper(UserMapper.class);
      Example<User> example = new Example();
      example.createCriteria().andLike(User::getUserName, "殷%");
      example.setOrderByClause("id desc");
      Assert.assertEquals(5, exampleMapper.countByExample(example));
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testExampleUseCondition() {
    try (SqlSession sqlSession = getSqlSession()) {
      ExampleMapper<User, Example<User>> exampleMapper = sqlSession.getMapper(UserMapper.class);
      Example<User> example = new Example<>();

      User user = new User();
      user.setUserName("殷%");

      example.createCriteria()
          .andNotEqualTo(Objects.nonNull(user.getId()), User::getId, user.getId())
          .andLike(Utils.isNotBlank(user.getUserName()), User::getUserName, user.getUserName())
          .andEqualTo(Utils.isNotBlank(user.getSex()), User::getSex, user.getSex());
      Assert.assertEquals(5, exampleMapper.countByExample(example));
    }
  }

  @Test
  public void testExampleUseSelective() {
    try (SqlSession sqlSession = getSqlSession()) {
      ExampleMapper<User, Example<User>> exampleMapper = sqlSession.getMapper(UserMapper.class);
      Example<User> example = new Example<>();

      User user = new User();
      user.setUserName("殷%");
      user.setSex("女");

      example.createCriteriaSelective()
          .andLike(User::getUserName, user.getUserName())
          .andEqualTo(User::getId, user.getId())
          .andNotEqualTo(User::getSex, user.getSex());

      Assert.assertEquals(3, exampleMapper.countByExample(example));
    }
  }


}


