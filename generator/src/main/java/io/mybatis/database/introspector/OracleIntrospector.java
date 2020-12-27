/*
 * Copyright 2020 the original author or authors.
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

package io.mybatis.database.introspector;

import io.mybatis.common.util.Utils;
import io.mybatis.database.DatabaseConfig;
import io.mybatis.database.utils.DBMetadataUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class OracleIntrospector extends DatabaseIntrospector {

  public OracleIntrospector(DBMetadataUtils dbMetadataUtils) {
    super(dbMetadataUtils);
  }

  public OracleIntrospector(DBMetadataUtils dbMetadataUtils, boolean forceBigDecimals, boolean useCamelCase) {
    super(dbMetadataUtils, forceBigDecimals, useCamelCase);
  }

  /**
   * 获取表名和注释映射
   *
   * @param config
   * @return
   * @throws SQLException
   */
  @Override
  protected Map<String, String> getTableComments(DatabaseConfig config) throws SQLException {
    Map<String, String> answer = new HashMap<String, String>();
    try {
      StringBuilder sqlBuilder = new StringBuilder("select table_name tname,comments from all_tab_comments where comments is not null ");
      if (Utils.isNotEmpty(config.getSchemaPattern())) {
        sqlBuilder.append(" and owner like :1 ");
      }
      sqlBuilder.append("order by tname ");
      PreparedStatement preparedStatement = dbMetadataUtils.getConnection().prepareStatement(sqlBuilder.toString());
      if (Utils.isNotEmpty(config.getSchemaPattern())) {
        preparedStatement.setString(1, config.getSchemaPattern());
      }
      ResultSet rs = preparedStatement.executeQuery();
      while (rs.next()) {
        answer.put(rs.getString(dbMetadataUtils.convertLetterByCase("TNAME")), rs.getString(dbMetadataUtils.convertLetterByCase("COMMENTS")));
      }
      closeResultSet(rs);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return answer;
  }

  /**
   * 获取表字段注释
   *
   * @param config
   * @return
   * @throws SQLException
   */
  @Override
  protected Map<String, Map<String, String>> getColumnComments(DatabaseConfig config) throws SQLException {
    Map<String, Map<String, String>> answer = new HashMap<String, Map<String, String>>();
    try {
      StringBuilder sqlBuilder = new StringBuilder("select table_name tname,column_name cname,comments from all_col_comments ");
      sqlBuilder.append("where comments is not null ");
      if (Utils.isNotEmpty(config.getSchemaPattern())) {
        sqlBuilder.append(" and owner like :1 ");
      }
      sqlBuilder.append("order by table_name,column_name ");

      PreparedStatement preparedStatement = dbMetadataUtils.getConnection().prepareStatement(sqlBuilder.toString());
      if (Utils.isNotEmpty(config.getSchemaPattern())) {
        preparedStatement.setString(1, config.getSchemaPattern());
      }
      ResultSet rs = preparedStatement.executeQuery();
      while (rs.next()) {
        String tname = rs.getString(dbMetadataUtils.convertLetterByCase("TNAME"));
        if (!answer.containsKey(tname)) {
          answer.put(tname, new HashMap<String, String>());
        }
        answer.get(tname).put(rs.getString(dbMetadataUtils.convertLetterByCase("CNAME")), rs.getString(dbMetadataUtils.convertLetterByCase("COMMENTS")));
      }
      closeResultSet(rs);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return answer;
  }
}
