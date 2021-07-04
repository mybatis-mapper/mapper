## 8. JPA 注解支持

### 注解介绍

对 JPA 注解的支持非常有限，仅支持以下注解的部分属性：
- `@Table`: 用于实体类，必须指定该注解，否则不会被识别为实体类
  - name: 设置表名
    
- `@Column`: 设置字段的列信息
  - name: 列名
  - insertable: 是否可插入
  - updatable: 是否可更新
  - scale: 小数位数

- `@Transient`: 排除字段，没有标记该字段的所有字段都会作为表字段处理

- `@OrderBy`: 设置字段排序，空值时为 `ASC`，除此之外，值只能为 `ASC` 或 `DESC`

>除了上面提到的有限支持外，其他的 JPA 注解都不支持，如果有好的建议或者应用场景，可以提交 issue 或者 PR 增加额外的支持。

除了 JPA 这几个注解外，还支持 `@Entity.Table` 和 `@Entity.Column` 注解，这些注解可以混用，相同含义的配置中，JPA优先级更高。

### JPA 示例

```java
@Table(name = "user")
public class User {
  @Id
  @Column
  private Long   id;
  @Column(name = "name")
  private String username;
  @Column
  @Entity.Column(selectable = false)
  private String sex;
  //忽略其他
}
```

### 扩展实现

#### 1. 查找实体类类型

首先通过 `JpaEntityClassFinder` 支持添加了 `javax.persistence.Table` 注解的实体类。

```java
public class JpaEntityClassFinder extends GenericEntityClassFinder {

  @Override
  public boolean isEntityClass(Class<?> clazz) {
    return clazz.isAnnotationPresent(Table.class);
  }

  @Override
  public int getOrder() {
    return super.getOrder() + 100;
  }
}
```

#### 2. 构建 EntityTable 信息

然后通过 `JpaEntityTableFactory` 读取 `javax.persistence.Table` 注解的配置信息。

```java
public class JpaEntityTableFactory implements EntityTableFactory {

  @Override
  public EntityTable createEntityTable(Class<?> entityClass, Chain chain) {
    EntityTable entityTable = chain.createEntityTable(entityClass);
    if (entityClass.isAnnotationPresent(Table.class)) {
      Table table = entityClass.getAnnotation(Table.class);
      if(entityTable == null) {
        entityTable = EntityTable.of(entityClass);
      }
      if (!table.name().isEmpty()) {
        entityTable.table(table.name());
      }
    }
    return entityTable;
  }

  @Override
  public int getOrder() {
    return EntityTableFactory.super.getOrder() + 100;
  }

}
```
在实现中先执行了 `chain.createEntityTable` 调用工厂链的后续方法创建 `EntityTable`，因此 JPA 注解实现支持和 `@Entity.Table` 注解混用。

如果 `chain` 方法没有返回 `EntityTable`，就根据 `javax.persistence.Table` 注解创建。

如果返回了 `EntityTable`，就用 `javax.persistence.Table` 注解的配置覆盖 `EntityTable` 的配置，也就是 JPA 注解优先级高于默认的 `@Entity.Table` 注解。

#### 3. 构建 EntityColumn 信息

最后通过 `JpaEntityColumnFactory` 构建 `EntityColumn` 信息，这部分逻辑和上面类似，仍然支持 `@Entity.Column` 注解，而且 JPA 的优先级更高。

```java
public class JpaEntityColumnFactory implements EntityColumnFactory {

  @Override
  public Optional<List<EntityColumn>> createEntityColumn(EntityTable entityTable, EntityField field, Chain chain) {
    //代码太长，省略
  }

  @Override
  public int getOrder() {
    return EntityColumnFactory.super.getOrder() + 100;
  }

}
```

在上面具体代码的实现中，会先判断 `@Transient` 注解，只要标记了该注解就会忽略。

然后是读取 JPA 中的 `@Column` 注解，支持下面的属性：
- name: 列名
- insertable: 是否可插入
- updatable: 是否可更新
- scale: 小数位数

还支持 JPA 的 `@Id` 设置主键字段。

支持 `@OrderBy` 设置字段的排序，这里有一定的限制，不能指定多个字段，只能为空（默认 `ASC`)或者为 `ASC` 或 `DESC`。

除了上面提到的有限支持外，其他的 JPA 注解都不支持，如果有好的建议或者应用场景，可以提交 issue 或者 PR 增加额外的支持。