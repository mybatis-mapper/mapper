package io.mybatis.provider.jpa;

import io.mybatis.provider.BaseTest;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

public class UserMapperTest extends BaseTest {

  @Test
  public void testSelectById() {
    try (SqlSession sqlSession = getSqlSession()) {
      UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
      User user = userMapper.getById(1L);
      Assert.assertNotNull(user);
      Assert.assertEquals("张无忌", user.getUsername());
      Assert.assertNull(user.getSex());
    }
  }

}
