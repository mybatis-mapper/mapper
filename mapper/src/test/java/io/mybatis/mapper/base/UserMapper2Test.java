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
import io.mybatis.mapper.UserMapper2;
import io.mybatis.mapper.example.Example;
import io.mybatis.mapper.fn.Fn;
import io.mybatis.mapper.model.User;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class UserMapper2Test extends BaseMapperTest {

  @Test
  public void testInsert() {
    SqlSession sqlSession = getSqlSession();
    try {
      UserMapper2 mapper = sqlSession.getMapper(UserMapper2.class);
      User user = new User();
      user.setUserName("测试");
      mapper.insert(user);
      Assert.assertNotNull(user.getId());
      mapper.selectByPrimaryKey(user.getId());
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
      UserMapper2 mapper = sqlSession.getMapper(UserMapper2.class);
      User user = new User();
      user.setSex("性别");
      mapper.insertSelective(user);
      Assert.assertNotNull(user.getId());
      Optional<User> userOptional = mapper.selectByPrimaryKey(user.getId());
      Assert.assertTrue(userOptional.isPresent());
      Assert.assertEquals("DEFAULT", userOptional.get().getUserName());
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testUpdateByPrimaryKeySelectiveWithForceFields() {
    SqlSession sqlSession = getSqlSession();
    try {
      UserMapper2 mapper = sqlSession.getMapper(UserMapper2.class);
      User user = new User();
      user.setId(1L);
      user.setUserName("男主角");
      Assert.assertEquals(1, mapper.updateByPrimaryKeySelective(user));
      user = mapper.selectByPrimaryKey(1L).get();
      Assert.assertEquals("男主角", user.getUserName());
      Assert.assertNotNull(user.getSex());
      user.setSex(null);
      Assert.assertEquals(1, mapper.updateByPrimaryKeySelectiveWithForceFields(user, Fn.of(User::getSex)));
      user = mapper.selectByPrimaryKey(1L).get();
      Assert.assertEquals("男主角", user.getUserName());
      Assert.assertNull(user.getSex());
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testSelectByFieldList() {
    SqlSession sqlSession = getSqlSession();
    try {
      UserMapper2 mapper = sqlSession.getMapper(UserMapper2.class);
      List<User> users = mapper.selectByFieldList(User::getUserName, Arrays.asList("张无忌", "赵敏", "周芷若"));
      Assert.assertEquals(3, users.size());
      Assert.assertEquals("张无忌", users.get(0).getUserName());
      Assert.assertEquals("赵敏", users.get(1).getUserName());
      Assert.assertEquals("周芷若", users.get(2).getUserName());
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testDeleteByFieldList() {
    SqlSession sqlSession = getSqlSession();
    try {
      UserMapper2 mapper = sqlSession.getMapper(UserMapper2.class);
      Assert.assertEquals(3, mapper.deleteByFieldList(User::getUserName, Arrays.asList("张无忌", "赵敏", "周芷若")));
      Assert.assertFalse(mapper.selectByPrimaryKey(1L).isPresent());
      Assert.assertFalse(mapper.selectByPrimaryKey(2L).isPresent());
      Assert.assertFalse(mapper.selectByPrimaryKey(3L).isPresent());
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testOrCondition(){
    try(SqlSession sqlSession = getSqlSession()){
      UserMapper2 mapper = sqlSession.getMapper(UserMapper2.class);
      Example<User> example = mapper.example();
      example.createCriteria()
              .andEqualTo(User::getSex,"男")
              .andOr(example.orPart()
                              .andLike(User::getUserName,"杨%"),
                      example.orPart()
                              .andLike(User::getUserName,"俞%")
                              .andLike(User::getUserName,"%舟"));
      Assert.assertEquals(2,mapper.countByExample(example));
    }
  }
}


