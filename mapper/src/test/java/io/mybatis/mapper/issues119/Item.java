package io.mybatis.mapper.issues119;

import io.mybatis.provider.Entity;

@Entity.Table(value = "yahoo_items")
public class Item extends CommonEntity {
  String itemId;
  // xxx

  public String getItemId() {
    return itemId;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }
}
