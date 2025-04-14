package io.mybatis.mapper.issues119;

import io.mybatis.mapper.BaseMapperTest;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

public class ItemTest extends BaseMapperTest {

  @Test
  public void testUpdateByPrimaryKeySelective() {
    SqlSession sqlSession = getSqlSession();
    try {
      ItemMapper itemMapper = sqlSession.getMapper(ItemMapper.class);
      Item item = new Item();
      item.setItemId("123");
      item.setDataId("456");
      Assert.assertEquals(0, itemMapper.updateByPrimaryKeySelective(item));
      sqlSession.rollback();
    } finally {
      //不要忘记关闭sqlSession
      sqlSession.close();
    }
  }
}
