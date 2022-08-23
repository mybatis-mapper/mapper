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

package io.mybatis.mapper.cursor;

import io.mybatis.mapper.BaseMapperTest;
import io.mybatis.mapper.UserMapper;
import io.mybatis.mapper.example.Example;
import io.mybatis.mapper.model.User;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

public class UserCursorMapperTest extends BaseMapperTest {

  @Test
  public void testSelectCursor() {
    SqlSession sqlSession = getSqlSession();
    try {
      CursorMapper<User, Example<User>> mapper = sqlSession.getMapper(UserMapper.class);
      User user = new User();
      user.setSex("女");
      Cursor<User> userCursor = mapper.selectCursor(user);
      Iterator<User> userIterator = userCursor.iterator();
      int count = 0;
      while (userIterator.hasNext()) {
        count++;
        User u = userIterator.next();
        System.out.println(u.getUserName());
        Assert.assertEquals(count, userCursor.getCurrentIndex() + 1);
      }
      Assert.assertEquals(16, count);
      Assert.assertTrue(userCursor.isConsumed());
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testSelectCursorByExample() {
    SqlSession sqlSession = getSqlSession();
    try {
      CursorMapper<User, Example<User>> mapper = sqlSession.getMapper(UserMapper.class);
      Example<User> example = new Example<>();
      example.createCriteria().andEqualTo(User::getSex, "女").andLessThan(User::getId, 10);
      Cursor<User> userCursor = mapper.selectCursorByExample(example);
      Iterator<User> userIterator = userCursor.iterator();
      int count = 0;
      while (userIterator.hasNext()) {
        count++;
        User u = userIterator.next();
        System.out.println(u.getUserName());
        Assert.assertEquals(count, userCursor.getCurrentIndex() + 1);
      }
      Assert.assertEquals(5, count);
      Assert.assertTrue(userCursor.isConsumed());
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

}


