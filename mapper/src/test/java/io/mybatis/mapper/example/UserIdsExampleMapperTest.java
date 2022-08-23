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

import io.mybatis.mapper.BaseMapperTest;
import io.mybatis.mapper.UserIdsMapper;
import io.mybatis.mapper.model.UserIds;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class UserIdsExampleMapperTest extends BaseMapperTest {

  @Test
  public void testDeleteByExample() {
    SqlSession sqlSession = getSqlSession();
    try {
      ExampleMapper<UserIds, Example<UserIds>> exampleMapper = sqlSession.getMapper(UserIdsMapper.class);
      Example<UserIds> example = new Example();
      example.createCriteria().andGreaterThan(UserIds::getId2, 1L);
      Assert.assertEquals(3, exampleMapper.deleteByExample(example));
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
      ExampleMapper<UserIds, Example<UserIds>> exampleMapper = sqlSession.getMapper(UserIdsMapper.class);
      Example<UserIds> example = new Example();
      example.createCriteria().andEqualTo(UserIds::getId1, 1L).andEqualTo(UserIds::getId2, 1L);
      UserIds user = new UserIds();
      user.setId1(1L);
      user.setId2(0L);
      user.setName("男主角");
      Assert.assertEquals(1, exampleMapper.updateByExample(user, example));
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
      ExampleMapper<UserIds, Example<UserIds>> exampleMapper = sqlSession.getMapper(UserIdsMapper.class);
      Example<UserIds> example = new Example();
      example.createCriteria().andLessThanOrEqualTo(UserIds::getId1, 1L);
      UserIds user = new UserIds();
      user.setName("主角");
      Assert.assertEquals(4, exampleMapper.updateByExampleSelective(user, example));
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
      ExampleMapper<UserIds, Example<UserIds>> exampleMapper = sqlSession.getMapper(UserIdsMapper.class);
      Example<UserIds> example = new Example();
      example.createCriteria().andLike(UserIds::getName, "张%");
      example.setOrderByClause("id2 desc");
      List<UserIds> users = exampleMapper.selectByExample(example);
      Assert.assertEquals(4, users.size());
      Assert.assertEquals("张无忌4", users.get(0).getName());
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test(expected = TooManyResultsException.class)
  public void testSelectOneByExample() {
    SqlSession sqlSession = getSqlSession();
    try {
      ExampleMapper<UserIds, Example<UserIds>> exampleMapper = sqlSession.getMapper(UserIdsMapper.class);
      Example<UserIds> example = new Example();
      example.createCriteria().andEqualTo(UserIds::getId1, 1L);
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
      ExampleMapper<UserIds, Example<UserIds>> exampleMapper = sqlSession.getMapper(UserIdsMapper.class);
      Example<UserIds> example = new Example();
      example.createCriteria().andEqualTo(UserIds::getId1, 1L);
      example.setOrderByClause("id desc");
      Assert.assertEquals(4, exampleMapper.countByExample(example));
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

}


