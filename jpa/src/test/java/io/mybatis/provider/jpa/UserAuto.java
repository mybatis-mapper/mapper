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

package io.mybatis.provider.jpa;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Entity
public class UserAuto {
  @Id
  private Integer id;
  private String  userName;
  @Convert(converter = AddressTypeHandler.class)
  private Address address;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
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
