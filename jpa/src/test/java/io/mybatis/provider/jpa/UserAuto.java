package io.mybatis.provider.jpa;

import javax.persistence.Id;

public class UserAuto {
  @Id
  private int    id;
  private String userName;
  private String address;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }
}
