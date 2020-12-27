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

package io.mybatis.mapper.list;

import io.mybatis.mapper.BaseMapperTest;
import io.mybatis.mapper.UserMapper;
import io.mybatis.mapper.model.User;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class UserListMapperTest extends BaseMapperTest {

  @Test
  public void testInsertList() {
    SqlSession sqlSession = getSqlSession();
    try {
      UserMapper insertListMapper = sqlSession.getMapper(UserMapper.class);
      List<User> users = new ArrayList<>(10);
      for (int i = 0; i < 10; i++) {
        User user = new User();
        user.setUserName("测试" + i);
        users.add(user);
      }
      Assert.assertEquals(10, insertListMapper.insertList(users));
      users.stream().map(User::getId).forEach(System.out::println);
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

}


