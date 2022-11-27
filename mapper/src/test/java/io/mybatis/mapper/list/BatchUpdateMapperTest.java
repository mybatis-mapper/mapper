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

import io.mybatis.mapper.H2BaseMapperTest;
import io.mybatis.mapper.TestBatchUpdateMapper;
import io.mybatis.mapper.model.UserIds;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BatchUpdateMapperTest extends H2BaseMapperTest {

  @Test
  public void testUpdateList() {
    SqlSession sqlSession = getSqlSession();
    try {
      TestBatchUpdateMapper batchUpdateUserIdsMapper = sqlSession.getMapper(TestBatchUpdateMapper.class);
      List<UserIds> users = new ArrayList<>(10);
      for (int i = 0; i < 2; i++) {
        UserIds user = new UserIds();
        user.setId1(1L);
        user.setId2(i + 1L);
        user.setName("测试" + i);
        users.add(user);
      }
      Assert.assertEquals(2, batchUpdateUserIdsMapper.updateList(users));
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testUpdateListSelective() {
    SqlSession sqlSession = getSqlSession();
    try {
      TestBatchUpdateMapper batchUpdateUserIdsMapper = sqlSession.getMapper(TestBatchUpdateMapper.class);

      List<UserIds> users = new ArrayList<>(10);
      UserIds user3 = new UserIds();
      user3.setId1(1L);
      user3.setId2(3L);
      user3.setName(null);
      users.add(user3);
      UserIds beforeData = batchUpdateUserIdsMapper.selectByPrimaryKey(user3).get();
      for (int i = 0; i < 2; i++) {
        UserIds user = new UserIds();
        user.setId1(1L);
        user.setId2(i + 1L);
        user.setName("测试" + i);
        users.add(user);
      }

      Assert.assertEquals(3, batchUpdateUserIdsMapper.updateListSelective(users));
      UserIds afterData = batchUpdateUserIdsMapper.selectByPrimaryKey(user3).get();
      Assert.assertEquals(beforeData.getName(), afterData.getName());
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }
}

