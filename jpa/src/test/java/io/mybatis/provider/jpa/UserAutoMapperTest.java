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

package io.mybatis.provider.jpa;

import io.mybatis.provider.BaseTest;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

public class UserAutoMapperTest extends BaseTest {

  @Test
  public void testSelectById() {
    try (SqlSession sqlSession = getSqlSession()) {
      UserAutoMapper userMapper = sqlSession.getMapper(UserAutoMapper.class);
      UserAuto user = userMapper.getById(1);
      Assert.assertNotNull(user);
      Assert.assertEquals("sjz", user.getUserName());
      Assert.assertNotNull(user.getAddress());
      Assert.assertEquals("河北省", user.getAddress().getSheng());
      Assert.assertEquals("石家庄市", user.getAddress().getShi());

      UserAuto.Address address = user.getAddress();
      address.setShi("秦皇岛市");
      user.setUserName("qhd");
      user.setId(null);
      userMapper.insertSelective(user);
      Assert.assertNotNull(user.getId());

      UserAuto qhd = userMapper.getById(user.getId());
      Assert.assertEquals("河北省", qhd.getAddress().getSheng());
      Assert.assertEquals("秦皇岛市", qhd.getAddress().getShi());
    }
  }

}
