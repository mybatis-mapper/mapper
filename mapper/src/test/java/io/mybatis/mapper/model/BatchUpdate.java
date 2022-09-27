package io.mybatis.mapper.model;

import io.mybatis.provider.Entity;

/**
 * @author dengsd
 * @date 2022/9/27 11:49
 */
@Entity.Table("user_batch")
public class BatchUpdate {
    @Entity.Column(id = true)
    private long id;
    private String name ;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
