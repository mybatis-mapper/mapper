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

package io.mybatis.mapper.list;

import io.mybatis.mapper.BaseMapperTest;
import io.mybatis.mapper.UserIdsMapper;
import io.mybatis.mapper.model.UserIds;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class UserIdsListMapperTest extends BaseMapperTest {

  @Test
  public void testInsertList() {
    SqlSession sqlSession = getSqlSession();
    try {
      UserIdsMapper insertListMapper = sqlSession.getMapper(UserIdsMapper.class);
      List<UserIds> users = new ArrayList<>(10);
      for (int i = 0; i < 10; i++) {
        UserIds user = new UserIds();
        //注释后使用genId自动生成，测试批量插入genId
        //user.setId1(2L);
        user.setId2((long) i);
        user.setName("测试" + i);
        users.add(user);
      }
      Assert.assertEquals(10, insertListMapper.insertList(users));
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

}


