package io.mybatis.common.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Utils单元测试用例
 */
public class UtilsTest {

  /**
   * 测试常见的空白字符
   */
  @Test
  public void testBlankChar() {
    Assert.assertTrue(Utils.isBlankChar(' '));
    Assert.assertTrue(Utils.isBlankChar('\n'));
    Assert.assertTrue(Utils.isBlankChar('\r'));
    Assert.assertTrue(Utils.isBlankChar('\t'));
    Assert.assertTrue(Utils.isBlankChar('\f'));

    Assert.assertTrue(Utils.isBlankChar('\u00A0'));
    Assert.assertTrue(Utils.isBlankChar('\ufeff'));
    Assert.assertTrue(Utils.isBlankChar('\u3000'));
    Assert.assertTrue(Utils.isBlankChar('\u202a'));
    // 处理来自Word的文本时，偶见此空白字符
    Assert.assertTrue(Utils.isBlankChar('\u200B'));

    Assert.assertFalse(Utils.isBlankChar('a'));
    Assert.assertFalse(Utils.isBlankChar('z'));
    Assert.assertFalse(Utils.isBlankChar('0'));
    Assert.assertFalse(Utils.isBlankChar('9'));
    Assert.assertFalse(Utils.isBlankChar('/'));
    Assert.assertFalse(Utils.isBlankChar('\\'));
  }
}
