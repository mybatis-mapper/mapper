package io.mybatis.mapper.logical;

import io.mybatis.mapper.BaseMapperTest;
import io.mybatis.mapper.UserMapper;
import io.mybatis.mapper.model.User;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author hzw
 */
public class LogicalMapperTest extends BaseMapperTest {

  @Test
  public void testSelect() {
    SqlSession sqlSession = getSqlSession();
    try {
      UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
      List<User> users = userMapper.selectList(null);
      Assert.assertEquals(53, users.size());
      Assert.assertEquals(users.size(), userMapper.selectCount(null));

      User user = new User();
      user.setUserName("黄衫女子");
      Assert.assertEquals(false, userMapper.selectOne(user).isPresent());
      users.stream().map(User::getId).forEach(System.out::println);
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

}
