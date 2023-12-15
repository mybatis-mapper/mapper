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

package io.mybatis.mapper.model;

import io.mybatis.provider.Entity;
import io.mybatis.provider.EntityColumn;
import io.mybatis.provider.EntityTable;
import io.mybatis.provider.keysql.GenId;

import java.util.concurrent.atomic.AtomicLong;

@Entity.Table("user_ids")
public class UserIds {

  public static class GenUserId implements GenId<Long> {
    AtomicLong index = new AtomicLong(10000);
    @Override
    public Long genId(EntityTable table, EntityColumn column) {
      return index.incrementAndGet();
    }
  }

  @Entity.Column(id = true, genIdExecuteBefore = true, genId = GenUserId.class)
  private Long   id1;
  @Entity.Column(id = true)
  private Long   id2;
  @Entity.Column
  private String name;

  public UserIds() {
  }

  public UserIds(Long id1, Long id2) {
    this.id1 = id1;
    this.id2 = id2;
  }

  public UserIds(Long id1, Long id2, String name) {
    this.id1 = id1;
    this.id2 = id2;
    this.name = name;
  }

  public Long getId1() {
    return id1;
  }

  public void setId1(Long id1) {
    this.id1 = id1;
  }

  public Long getId2() {
    return id2;
  }

  public void setId2(Long id2) {
    this.id2 = id2;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "UserIds{" +
        "id1=" + id1 +
        ", id2=" + id2 +
        ", name='" + name + '\'' +
        '}';
  }
}
