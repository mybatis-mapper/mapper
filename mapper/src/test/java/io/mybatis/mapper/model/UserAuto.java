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

package io.mybatis.mapper.model;

import io.mybatis.provider.Entity;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Entity.Table(value = "user_auto", autoResultMap = true)
public class UserAuto {
  @Entity.Column(id = true)
  private Long    id;
  @Entity.Column("user_name")
  private String  name;
  @Entity.Column(typeHandler = AddressTypeHandler.class)
  private Address address;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  @Override
  public String toString() {
    return "UserAuto{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", address=" + address +
        '}';
  }

  public static class Address {
    private String sheng;
    private String shi;

    public String getSheng() {
      return sheng;
    }

    public void setSheng(String sheng) {
      this.sheng = sheng;
    }

    public String getShi() {
      return shi;
    }

    public void setShi(String shi) {
      this.shi = shi;
    }

    @Override
    public String toString() {
      return sheng + "---" + shi;
    }
  }

  public static class AddressTypeHandler extends BaseTypeHandler<Address> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Address parameter, JdbcType jdbcType) throws SQLException {
      ps.setString(i, parameter.getSheng() + "/" + parameter.getShi());
    }

    private Address valueToAddress(String value) {
      String[] split = value.split("/");
      Address address = new Address();
      address.setSheng(split[0]);
      address.setShi(split[1]);
      return address;
    }

    @Override
    public Address getNullableResult(ResultSet rs, String columnName) throws SQLException {
      String string = rs.getString(columnName);
      return string == null ? null : valueToAddress(string);
    }

    @Override
    public Address getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
      String string = rs.getString(columnIndex);
      return string == null ? null : valueToAddress(string);
    }

    @Override
    public Address getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
      String string = cs.getString(columnIndex);
      return string == null ? null : valueToAddress(string);
    }
  }
}
