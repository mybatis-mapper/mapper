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

package io.mybatis.common.util;

import io.mybatis.common.core.Code;
import org.junit.Test;

import java.util.Locale;

public class I18nTest {

  @Test
  public void testCode() {
    Locale.setDefault(Locale.CHINA);
    org.junit.Assert.assertEquals("操作成功", Code.SUCCESS.getMessage());
    org.junit.Assert.assertEquals("操作失败", Code.FAILURE.getMessage());
    org.junit.Assert.assertEquals("未知错误", Code.UNKNOWN.getMessage());
    org.junit.Assert.assertEquals("保存失败", Code.SAVE_FAILURE.getMessage());
    org.junit.Assert.assertEquals("修改失败", Code.UPDATE_FAILURE.getMessage());
    org.junit.Assert.assertEquals("删除失败", Code.DELETE_FAILURE.getMessage());
  }

  @Test
  public void testDefault() {
    //使用当前操作系统默认值
    org.junit.Assert.assertEquals("操作成功", I18n.message(Code.CODE_BUNDLE, "00000"));
    //语言无法匹配时，使用默认值
    org.junit.Assert.assertEquals("操作成功", I18n.message(new Locale("hello", "WORLD"), Code.CODE_BUNDLE, "00000"));
  }

  @Test
  public void testZh() {
    org.junit.Assert.assertEquals("操作成功", I18n.message(new Locale("zh", ""), Code.CODE_BUNDLE, "00000"));
  }

  @Test
  public void testZhCN() {
    org.junit.Assert.assertEquals("操作成功", I18n.message(new Locale("zh", "CN"), Code.CODE_BUNDLE, "00000"));
  }

  @Test
  public void testEn() {
    org.junit.Assert.assertEquals("Success", I18n.message(new Locale("en", ""), Code.CODE_BUNDLE, "00000"));
  }

  @Test
  public void testEnUS() {
    org.junit.Assert.assertEquals("Success", I18n.message(new Locale("en", "US"), Code.CODE_BUNDLE, "00000"));
  }

  @Test
  public void testEnOthers() {
    org.junit.Assert.assertEquals("Success", I18n.message(new Locale("en", "Others"), Code.CODE_BUNDLE, "00000"));
  }

  @Test
  public void testLanguage() {
    I18n.Language defLanguage = I18n.language(new Locale("hello", "WORLD"), Code.CODE_BUNDLE);
    org.junit.Assert.assertEquals("操作成功", defLanguage.message("00000"));

    I18n.Language zhLanguage = I18n.language(new Locale("zh", ""), Code.CODE_BUNDLE);
    org.junit.Assert.assertEquals("操作成功", zhLanguage.message("00000"));

    I18n.Language enLanguage = I18n.language(new Locale("en", "Others"), Code.CODE_BUNDLE);
    org.junit.Assert.assertEquals("Success", enLanguage.message("00000"));
  }


}
