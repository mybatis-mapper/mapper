package io.mybatis.mapper.issues119;

import io.mybatis.provider.Entity;

public class CommonEntityUuidId extends CommonDateBase {
  @Entity.Column(id = true, updatable = false)
  private String dataId;

  public String getDataId() {
    return dataId;
  }

  public void setDataId(String dataId) {
    this.dataId = dataId;
  }
}
