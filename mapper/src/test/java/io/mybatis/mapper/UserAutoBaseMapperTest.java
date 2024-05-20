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

package io.mybatis.mapper;

import io.mybatis.mapper.example.Example;
import io.mybatis.mapper.example.ExampleMapper;
import io.mybatis.mapper.model.UserAuto;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class UserAutoBaseMapperTest extends BaseMapperTest {

  @Test
  public void testUpdateByExampleSetValues() {
    SqlSession sqlSession = getSqlSession();
    try {
      ExampleMapper<UserAuto, Example<UserAuto>> exampleMapper = sqlSession.getMapper(UserAutoBaseMapper.class);
      Example<UserAuto> example = new Example();
      example.createCriteria().andEqualTo(UserAuto::getId, 1L);
      example.set(UserAuto::getAddress, new UserAuto.Address("河北省", "秦皇岛市"));

      Assert.assertEquals(1, exampleMapper.updateByExampleSetValues(example));
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testUpdateByExampleWrapperSetValues() {
    SqlSession sqlSession = getSqlSession();
    try {
      UserAutoBaseMapper userAutoMapper = sqlSession.getMapper(UserAutoBaseMapper.class);
      int update = userAutoMapper.wrapper()
          .set(UserAuto::getAddress, new UserAuto.Address("河北省", "秦皇岛市"))
          .eq(UserAuto::getId, 1L).update();
      Assert.assertEquals(1, update);
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testExampleWhereTypeHandler() {
    SqlSession sqlSession = getSqlSession();
    try {
      UserAutoBaseMapper userAutoMapper = sqlSession.getMapper(UserAutoBaseMapper.class);
      Example<UserAuto> example = userAutoMapper.example();

      UserAuto.Address address = new UserAuto.Address("河北省", "石家庄市");
      example.createCriteria().andEqualTo(UserAuto::getAddress, address);
      List<UserAuto> userAutos = userAutoMapper.selectByExample(example);
      Assert.assertEquals(1, userAutos.size());

      Assert.assertTrue(userAutoMapper.selectOneByExample(example).isPresent());

      Assert.assertEquals(1, userAutoMapper.countByExample(example));

      UserAuto.Address address2 = new UserAuto.Address("河北省", "邯郸市");
      List<UserAuto.Address> addresses = Arrays.asList(address, address2);

      example = userAutoMapper.example();
      example.createCriteria().andBetween(UserAuto::getAddress, address, address2);
      userAutos = userAutoMapper.selectByExample(example);
      Assert.assertEquals(3, userAutos.size());

      userAutos = userAutoMapper.selectByFieldList(UserAuto::getAddress, addresses);
      Assert.assertEquals(2, userAutos.size());

      example = userAutoMapper.example();
      example.createCriteria().andIn(UserAuto::getAddress, addresses);


      Assert.assertEquals(2, userAutoMapper.deleteByExample(example));

      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testExampleWrapperWhereTypeHandler() {
    SqlSession sqlSession = getSqlSession();
    try {
      UserAutoBaseMapper userAutoMapper = sqlSession.getMapper(UserAutoBaseMapper.class);

      UserAuto.Address address = new UserAuto.Address("河北省", "石家庄市");
      List<UserAuto> userAutos = userAutoMapper.wrapper().eq(UserAuto::getAddress, address).list();
      Assert.assertEquals(1, userAutos.size());

      Assert.assertTrue(userAutoMapper.wrapper().eq(UserAuto::getAddress, address).one().isPresent());

      Assert.assertEquals(1, userAutoMapper.wrapper().eq(UserAuto::getAddress, address).count());

      Assert.assertEquals(2, userAutoMapper.wrapper()
          .in(UserAuto::getAddress, Arrays.asList(address, new UserAuto.Address("河北省", "邯郸市"))).delete());

      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }


}


