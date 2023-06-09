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

package io.mybatis.mapper.wrapper;

import io.mybatis.mapper.BaseMapperTest;
import io.mybatis.mapper.model.User;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserWrapperMapperTest extends BaseMapperTest {

  @Test
  public void testInsertNullable() {
    SqlSession sqlSession = getSqlSession();
    try {
      UserWrapperWrapperMapper mapper = sqlSession.getMapper(UserWrapperWrapperMapper.class);
      // ==================testInsertNullable userName插入为null, 覆盖了默认数据库DEFAULT值
      User user = new User();
      mapper.insertNullable(user);
      Assert.assertNotNull(user.getId());
      user = mapper.selectById(user.getId()).get();
      Assert.assertNull(user.getUserName());
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testInsert() {
    SqlSession sqlSession = getSqlSession();
    try {
      UserWrapperWrapperMapper mapper = sqlSession.getMapper(UserWrapperWrapperMapper.class);
      // ==================testInsert userName未插入, 默认为数据库DEFAULT值
      User user = new User();
      user.setSex("性别");
      mapper.insert(user);
      Assert.assertNotNull(user.getId());
      Optional<User> userOptional = mapper.selectById(user.getId());
      Assert.assertTrue(userOptional.isPresent());
      Assert.assertEquals("DEFAULT", userOptional.get().getUserName());
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testUpdateByEntityId() {
    SqlSession sqlSession = getSqlSession();
    try {
      UserWrapperWrapperMapper mapper = sqlSession.getMapper(UserWrapperWrapperMapper.class);
      // ==================test updateById 测试只更新userName, 未更新sex
      User userBefore = mapper.selectById(1L).get();
      User user = new User();
      user.setId(1L);
      user.setUserName("男主角");
      user.setSex(null);
      Assert.assertEquals(1, mapper.updateById(user));
      user = mapper.selectById(1L).get();
      Assert.assertEquals("男主角", user.getUserName());
      Assert.assertEquals(userBefore.getSex(), user.getSex());
      // ==================test updateNullableById 测试更新sex为null
      user.setSex(null);
      mapper.updateNullableById(user);
      user = mapper.selectById(1L).get();
      Assert.assertNull(user.getSex());
      // ==================test update entity中sex为null了, 测试强制更新sex为女
      mapper.update(user, mapper.sql()
          .set(User::getSex, "女").where()
          .eq(User::getId, user.getId()).build());
      user = mapper.selectById(1L).get();
      Assert.assertEquals("女", user.getSex());
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
      UserWrapperWrapperMapper mapper = sqlSession.getMapper(UserWrapperWrapperMapper.class);
      // ==================test selectList
      List<User> users = mapper.selectList(mapper.where().in(User::getUserName, Arrays.asList("张无忌", "赵敏", "周芷若")).build());
      Assert.assertEquals(3, users.size());
      Assert.assertEquals("张无忌", users.get(0).getUserName());
      Assert.assertEquals("赵敏", users.get(1).getUserName());
      Assert.assertEquals("周芷若", users.get(2).getUserName());
      // ==================test selectByIds
      List<User> userList = mapper.selectByIds(1L, 2L, 3L);
      Assert.assertEquals(3, userList.size());
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testDeleteByFieldList() {
    SqlSession sqlSession = getSqlSession();
    try {
      UserWrapperWrapperMapper mapper = sqlSession.getMapper(UserWrapperWrapperMapper.class);
      // ==================test deleteById
      Assert.assertEquals(1, mapper.deleteById(1L));
      sqlSession.rollback();
      // ==================test delete
      Assert.assertEquals(3, mapper.delete(mapper.where().in(User::getUserName, Arrays.asList("张无忌", "赵敏", "周芷若")).build()));
      Assert.assertFalse(mapper.selectById(1L).isPresent());
      Assert.assertFalse(mapper.selectById(2L).isPresent());
      Assert.assertFalse(mapper.selectById(3L).isPresent());
      sqlSession.rollback();
      // ==================test deleteByIds
      Assert.assertEquals(3, mapper.deleteByIds(1L, 2L, 3L));
      Assert.assertFalse(mapper.selectById(1L).isPresent());
      Assert.assertFalse(mapper.selectById(2L).isPresent());
      Assert.assertFalse(mapper.selectById(3L).isPresent());
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testOrCondition() {
    try (SqlSession sqlSession = getSqlSession()) {
      UserWrapperWrapperMapper mapper = sqlSession.getMapper(UserWrapperWrapperMapper.class);
      Wrapper<User> sql = mapper.sql();
      sql.where()
          .eq(User::getSex, "男")
          .and(or1 -> or1
                  .like(User::getUserName, "杨%"),
              or2 -> or2
                  .like(User::getUserName, "俞%")
                  .like(User::getUserName, "舟%"));
      // ==================test count
      Assert.assertEquals(2, mapper.count(sql));
      mapper.selectList(null);
      mapper.selectList(new Wrapper<>());
      mapper.count(null);
      mapper.count(new Wrapper<>());
    }
  }

  @Test
  public void testExampleWrapper() {
    try (SqlSession sqlSession = getSqlSession()) {
      UserWrapperWrapperMapper mapper = sqlSession.getMapper(UserWrapperWrapperMapper.class);
      //查询 "sex=男 or name like '杨%' 的数量
      long count = mapper.where().eq(User::getSex, "男")
          .and(
              c -> c.startsWith(User::getUserName, "杨")
          ).execute(mapper::count);
      Assert.assertEquals(1, count);
      //查询 "sex=男 and (name like '杨%' or (name like '俞%' and name like '%舟')) 的数量
      count = mapper.where().eq(User::getSex, "男")
          .and(
              c -> c.startsWith(User::getUserName, "杨"),
              c -> c.startsWith(User::getUserName, "俞").endsWith(User::getUserName, "舟")
          ).execute(mapper::count);
      Assert.assertEquals(2, count);
      // ==================test selectList
      //查询 name 和 sex 列，条件为 (name like '杨%' or sex = '男') or ( id > 1 and id <= 16 and sex = '女' ) 的数据
      List<User> users = mapper.sql()
          .select(User::getUserName, User::getSex)
          .where().and(c -> c.startsWith(User::getUserName, "杨"),
              c -> c.eq(User::getSex, "男"))
          .or()
          .gt(User::getId, 1L)
          .le(User::getId, 16L)
          .eq(User::getSex, "女").execute(mapper::selectList);

      //构建的wrapper可以多次使用
      //查询条件为 id > 50 or id <= 5 or sex = '女'
      Wrapper<User> wrapper = mapper.where()
          .gt(User::getId, 50L)
          .or()
          .le(User::getId, 5L)
          .or()
          .eq(User::getSex, "女").build();
      // ==================test selectList
      //使用当前条件获取前5条数据
      users = mapper.selectList(new RowBounds(0, 5), wrapper);
      Assert.assertEquals(5, users.size());
      int idSize = mapper.selectList(wrapper).stream().map(User::getId).collect(Collectors.toSet()).size();
      //追加条件后查询数量
      count = wrapper.select(User::getId).distinct(true).where().execute(mapper::count);
      Assert.assertEquals(idSize, count);

      //根据条件"name=张无忌"，更新名字和性别
      Assert.assertEquals(1, (int) mapper.sql()
          .set(User::getUserName, "弓长无忌")
          .set(User::getSex, "M")
          .where().eq(User::getUserName, "张无忌").execute(new User(), mapper::update));
      //根据条件"sex=M"查询数量
      Assert.assertEquals(1, (long) mapper.where().eq(User::getSex, "M").execute(mapper::count));

      mapper.where()
          .eq(User::getSex, "女")
          .and(c -> c.gt(User::getId, 40), c -> c.lt(User::getId, 10))
          .or()
          .startsWith(User::getUserName, "张")
          .orderByAsc(User::getId).execute(mapper::selectList);

      mapper.where()
          .eq(false, User::getSex, "女")
          .and(c -> c.gt(User::getId, 40), c -> c.lt(false, User::getId, 10))
          .or()
          .startsWith(User::getUserName, "张")
          .orderByAsc(User::getId).execute(mapper::selectList);

      Optional<User> userOne = mapper.selectOne(mapper.where().like(User::getUserName, "张").build());
      userOne.orElseThrow(RuntimeException::new);

      try {
        Optional<User> userOneThrow = mapper.selectOneThrow(mapper.where().like(User::getUserName, "张").build());
      } catch (TooManyResultsException e) {
        Assert.assertTrue(true);
      }
    }
  }
}


