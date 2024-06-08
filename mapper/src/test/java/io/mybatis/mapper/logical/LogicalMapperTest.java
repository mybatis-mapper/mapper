package io.mybatis.mapper.logical;

import io.mybatis.mapper.BaseMapperTest;
import io.mybatis.mapper.UserMapper;
import io.mybatis.mapper.example.Example;
import io.mybatis.mapper.example.ExampleWrapper;
import io.mybatis.mapper.fn.Fn;
import io.mybatis.mapper.model.User;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

/**
 * @author hzw
 */
public class LogicalMapperTest extends BaseMapperTest {

  @Test
  public void testWrapper() {
    SqlSession sqlSession = getSqlSession();
    try {
      UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

      Assert.assertEquals(53, userMapper.wrapper().list().size());
      Assert.assertEquals(53, userMapper.wrapper().count());

      ExampleWrapper<User, Long> wrapper = userMapper.wrapper().select(User::getStatus, User::getId, User::getUserName)
          .eq(User::getUserName, "张翠山");
      wrapper.list().forEach(System.out::println);

      Assert.assertEquals(1, wrapper.list().size());
      Assert.assertEquals(1, wrapper.count());
      Assert.assertTrue(wrapper.one().isPresent());

      // logical delete+++
      int deleteCount = 0;
      Assert.assertEquals(1, wrapper.delete());
      deleteCount++;
      Assert.assertEquals(0, wrapper.delete());


      // logical delete ---
      Assert.assertEquals(53 - deleteCount, userMapper.wrapper().count());
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

  @Test
  public void testSelect() {
    SqlSession sqlSession = getSqlSession();
    try {
      UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

      Assert.assertEquals(53, userMapper.selectList(null).size());
      Assert.assertEquals(53, userMapper.selectCount(null));

      Assert.assertEquals(53, userMapper.selectByExample(null).size());
      Assert.assertEquals(53, userMapper.countByExample(null));


      try (Cursor<User> users = userMapper.selectCursor(null)) {
        users.forEach(System.out::println);
        Assert.assertEquals(52, users.getCurrentIndex());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      try (Cursor<User> users = userMapper.selectCursorByExample(null)) {
        users.forEach(System.out::println);
        Assert.assertEquals(52, users.getCurrentIndex());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      User user5 = new User();
      user5.setUserName("张翠山");

      userMapper.selectColumns(user5, Fn.of(User::getStatus, User::getId, User::getUserName)).forEach(System.out::println);

      Assert.assertEquals(1, userMapper.selectList(user5).size());
      Assert.assertEquals(1, userMapper.selectCount(user5));

      Example<User> e1 = new Example<>();
      e1.createCriteria()
          .andEqualTo(User::getUserName, "张翠山");

      Assert.assertEquals(1, userMapper.selectByExample(e1).size());
      Assert.assertEquals(1, userMapper.countByExample(e1));


      User user6 = new User();
      user6.setSex("男");
      long manSum = userMapper.selectCount(user6);
      Assert.assertEquals(manSum, userMapper.selectColumns(user6, Fn.of(User::getStatus, User::getId, User::getUserName)).size());

      Optional<User> user = userMapper.selectColumnsOne(user5, Fn.of(User::getStatus, User::getId, User::getUserName));
      Assert.assertTrue(user.isPresent());

      User user7 = new User();
      user7.setSex("?");
      Example<User> e2 = new Example<>();
      e2.createCriteria()
          .andEqualTo(User::getUserName, "殷素素");
      System.out.println(userMapper.selectOneByExample(e2));
      Assert.assertEquals(1, userMapper.updateByExampleSelective(user7, e2));
      System.out.println(userMapper.selectOneByExample(e2));

      User user8 = new User();
      user8.setUserName("周芷若1");
      user8.setId(3L);
      Assert.assertEquals(1, userMapper.updateByPrimaryKeySelective(user8));
      System.out.println(userMapper.selectByPrimaryKey(3L));

      Assert.assertEquals(1, userMapper.updateByPrimaryKey(user8));
      System.out.println(userMapper.selectByPrimaryKey(3L));


      // logical delete+++

      int deleteCount = 0;
      Assert.assertEquals(1, userMapper.delete(user5));
      deleteCount++;
      Assert.assertEquals(0, userMapper.delete(user5));


      Assert.assertEquals(1, userMapper.deleteByExample(e2));
      deleteCount++;
      Assert.assertEquals(0, userMapper.deleteByExample(e2));

      Assert.assertEquals(1, userMapper.deleteByPrimaryKey(3L));
      deleteCount++;
      Assert.assertEquals(0, userMapper.deleteByPrimaryKey(3L));


      // logical delete ---


      Assert.assertEquals(53 - deleteCount, userMapper.selectCount(null));


      Assert.assertEquals(53 - deleteCount, userMapper.selectList(null).size());
      Assert.assertEquals(53 - deleteCount, userMapper.selectCount(null));

      Assert.assertEquals(53 - deleteCount, userMapper.selectByExample(null).size());
      Assert.assertEquals(53 - deleteCount, userMapper.countByExample(null));


      Assert.assertEquals(0, userMapper.selectList(user5).size());
      Assert.assertEquals(0, userMapper.selectCount(user5));

      Assert.assertEquals(0, userMapper.selectByExample(e1).size());
      Assert.assertEquals(0, userMapper.countByExample(e1));

      try (Cursor<User> users = userMapper.selectCursor(null)) {
        users.forEach(System.out::println);
        Assert.assertEquals(52 - deleteCount, users.getCurrentIndex());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      try (Cursor<User> users = userMapper.selectCursorByExample(null)) {
        users.forEach(System.out::println);
        Assert.assertEquals(52 - deleteCount, users.getCurrentIndex());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      userMapper.selectColumns(user5, Fn.of(User::getStatus, User::getId, User::getUserName)).forEach(System.out::println);
      long manSumd = userMapper.selectCount(user6);
      Assert.assertEquals(manSum - 1, manSumd);
      Assert.assertEquals(manSumd, userMapper.selectColumns(user6, Fn.of(User::getStatus, User::getId, User::getUserName)).size());

      Assert.assertFalse(userMapper.selectColumnsOne(user5, Fn.of(User::getStatus, User::getId, User::getUserName)).isPresent());


      // update +++
      Assert.assertEquals(0, userMapper.updateByExampleSelective(user8, e2));
      Assert.assertEquals(0, userMapper.updateByPrimaryKey(user8));


      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }

}
