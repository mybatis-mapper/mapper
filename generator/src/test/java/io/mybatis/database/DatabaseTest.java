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

package io.mybatis.database;


import io.mybatis.database.utils.DBMetadataUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * @author liuzh
 */
public class DatabaseTest {

    public static void main(String[] args) {
        SimpleDataSource dataSource = new SimpleDataSource(
                Dialect.MYSQL8,
                "jdbc:mysql://localhost:3306/test",
                "root",
                ""
        );
        DBMetadataUtils dbMetadataUtils = null;
        try {
            dbMetadataUtils = new DBMetadataUtils(dataSource);

            List<IntrospectedTable> list = dbMetadataUtils.introspectTables(dbMetadataUtils.getDefaultConfig());

            for (IntrospectedTable table : list) {
                System.out.println(table.getName() + ":");
                for (IntrospectedColumn column : table.getAllColumns()) {
                    System.out.println(column.getName() + " - " +
                            column.getJdbcTypeName() + " - " +
                            column.getJavaProperty() + " - " +
                            column.getJavaProperty() + " - " +
                            column.getFullyQualifiedJavaType().getFullyQualifiedName() + " - " +
                            column.getRemarks());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
