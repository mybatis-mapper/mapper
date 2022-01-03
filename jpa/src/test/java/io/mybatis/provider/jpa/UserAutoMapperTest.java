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
      UserAuto user = userMapper.getById(1L);
      Assert.assertNotNull(user);
      Assert.assertEquals("sjz", user.getUserName());
      Assert.assertNotNull(user.getAddress());
    }
  }

}
