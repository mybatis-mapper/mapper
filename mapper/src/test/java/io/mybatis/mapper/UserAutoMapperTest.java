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

package io.mybatis.mapper;

import io.mybatis.mapper.example.Example;
import io.mybatis.mapper.fn.Fn;
import io.mybatis.mapper.model.UserAuto;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

public class UserAutoMapperTest extends BaseMapperTest {

  @Test
  public void testEntityInfo() {
    SqlSession sqlSession = getSqlSession();
    try {
      UserAutoMapper userAutoMapper = sqlSession.getMapper(UserAutoMapper.class);
      Assert.assertEquals(UserAuto.class, userAutoMapper.entityClass());
      Assert.assertEquals("user_auto", userAutoMapper.entityTable().table());
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testEntityMapper() {
    SqlSession sqlSession = getSqlSession();
    try {
      UserAutoMapper userAutoMapper = sqlSession.getMapper(UserAutoMapper.class);
      UserAuto user = new UserAuto();
      user.setName("测试");

      UserAuto.Address address = new UserAuto.Address();
      address.setSheng("河北省");
      address.setShi("秦皇岛市");
      user.setAddress(address);

      userAutoMapper.insert(user);
      Assert.assertNotNull(user.getId());
      Optional<UserAuto> userAuto = userAutoMapper.selectByPrimaryKey(user.getId());
      Assert.assertTrue(userAuto.isPresent());
      UserAuto auto = userAuto.get();
      Assert.assertNotNull(auto.getAddress());
      Assert.assertEquals("河北省", auto.getAddress().getSheng());
      Assert.assertEquals("秦皇岛市", auto.getAddress().getShi());

      long count = userAutoMapper.selectCount(new UserAuto());
      Assert.assertEquals(4, count);

      address.setShi("唐山市");
      Assert.assertEquals(1, userAutoMapper.updateByPrimaryKey(user));
      //typehandler作为查询字段
      UserAuto search = new UserAuto();
      search.setAddress(address);
      Optional<UserAuto> tsOptional = userAutoMapper.selectOne(search);
      Assert.assertTrue(tsOptional.isPresent());
      Assert.assertTrue(tsOptional.get().getId() > 3L);

      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testExampleMapper() {
    SqlSession sqlSession = getSqlSession();
    try {
      UserAutoMapper userAutoMapper = sqlSession.getMapper(UserAutoMapper.class);
      Example<UserAuto> example = userAutoMapper.example();
      example.createCriteria().andLike(UserAuto::getAddress, "河北省%");
      List<UserAuto> userAutos = userAutoMapper.selectByExample(example);
      Assert.assertTrue(userAutos.size() >= 3);
      UserAuto userAuto = userAutos.get(0);
      UserAuto.Address address = userAuto.getAddress();
      Assert.assertNotNull(address.getSheng());
      Assert.assertNotNull(address.getShi());

      address.setShi("秦皇岛市");
      example = userAutoMapper.example();
      example.createCriteria().andEqualTo(UserAuto::getId, userAuto.getId());
      Assert.assertEquals(1, userAutoMapper.updateByExample(userAuto, example));

      userAuto = userAutoMapper.selectByExample(example).get(0);
      Assert.assertNotNull(userAuto.getAddress());
      Assert.assertEquals("秦皇岛市", userAuto.getAddress().getShi());

      /**
       * 默认情况下 typehandler 在 example 查询条件中无效，但是可以把类型处理器配置为全局
       *
       * 例如：
       *
       * <typeHandler handler="io.mybatis.mapper.model.UserAuto$AddressTypeHandler"
       *                  javaType="io.mybatis.mapper.model.UserAuto$Address"/>
       *
       * 配置后，在 Example 查询条件中也可以有效（此时 address 字段上可以不指定 typeHandler）
       */
      example = userAutoMapper.example();
      example.createCriteria().andEqualTo(UserAuto::getAddress, userAuto.getAddress());
      userAutos = userAutoMapper.selectByExample(example);
      Assert.assertNotNull(userAutos);
      Assert.assertTrue(userAutos.size() == 1);
      Assert.assertEquals("秦皇岛市", userAutos.get(0).getAddress().getShi());
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }


  @Test
  public void testFnMapper() {
    SqlSession sqlSession = getSqlSession();
    try {
      UserAutoMapper userAutoMapper = sqlSession.getMapper(UserAutoMapper.class);
      UserAuto user = new UserAuto();
      user.setId(1L);
      user.setName("测试");
      Assert.assertEquals(1, userAutoMapper.updateByPrimaryKeySelectiveWithForceFields(user, Fn.of(UserAuto::getAddress)));
      user = userAutoMapper.selectByPrimaryKey(1L).get();
      Assert.assertNull(user.getAddress());

      user = new UserAuto();
      user.setId(1L);
      user = userAutoMapper.selectColumnsOne(user, Fn.of(UserAuto::getName)).get();
      Assert.assertNull(user.getId());
      Assert.assertNull(user.getAddress());
      Assert.assertNotNull(user.getName());
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }


}


