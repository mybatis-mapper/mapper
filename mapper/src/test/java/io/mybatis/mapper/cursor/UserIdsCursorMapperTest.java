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

package io.mybatis.mapper.cursor;

import io.mybatis.mapper.BaseMapperTest;
import io.mybatis.mapper.UserIdsMapper;
import io.mybatis.mapper.example.Example;
import io.mybatis.mapper.model.UserIds;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

public class UserIdsCursorMapperTest extends BaseMapperTest {

  @Test
  public void testSelectCursor() {
    SqlSession sqlSession = getSqlSession();
    try {
      CursorMapper<UserIds, Example<UserIds>> mapper = sqlSession.getMapper(UserIdsMapper.class);
      UserIds user = new UserIds();
      user.setId1(1L);
      Cursor<UserIds> userCursor = mapper.selectCursor(user);
      Iterator<UserIds> userIterator = userCursor.iterator();
      int count = 0;
      while (userIterator.hasNext()) {
        count++;
        UserIds u = userIterator.next();
        System.out.println(u.getName());
        Assert.assertEquals(count, userCursor.getCurrentIndex() + 1);
      }
      Assert.assertEquals(4, count);
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
      CursorMapper<UserIds, Example<UserIds>> mapper = sqlSession.getMapper(UserIdsMapper.class);
      Example<UserIds> example = new Example<>();
      example.createCriteria().andEqualTo(UserIds::getId1, 1).andLessThan(UserIds::getId2, 4);
      Cursor<UserIds> userCursor = mapper.selectCursorByExample(example);
      Iterator<UserIds> userIterator = userCursor.iterator();
      int count = 0;
      while (userIterator.hasNext()) {
        count++;
        UserIds u = userIterator.next();
        System.out.println(u.getName());
        Assert.assertEquals(count, userCursor.getCurrentIndex() + 1);
      }
      Assert.assertEquals(3, count);
      Assert.assertTrue(userCursor.isConsumed());
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

}


