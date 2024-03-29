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

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;

public class BaseMapperTest {
  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void init() {
    if (sqlSessionFactory == null) {
      try {
        Reader reader = Resources.getResourceAsReader("mybatis-config.xml");
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        reader.close();

        //创建数据库
        SqlSession session = null;
        try {
          session = sqlSessionFactory.openSession();
          Connection conn = session.getConnection();
          reader = Resources.getResourceAsReader("testdb.sql");
          ScriptRunner runner = new ScriptRunner(conn);
          runner.setLogWriter(null);
          runner.runScript(reader);
          reader.close();
        } finally {
          if (session != null) {
            session.close();
          }
        }
      } catch (IOException ignore) {
        ignore.printStackTrace();
      }
    }
  }

  public SqlSession getSqlSession() {
    return sqlSessionFactory.openSession();
  }

}
