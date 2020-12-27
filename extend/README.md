## 3. extend 模块

基于 **mybatis-mapper/provider** 核心部分扩展更新表和列的信息，可以直接配合 mapper 使用。

这个模块有两个作用：

1. 演示如何通过 SPI 扩展实体的配置信息。
2. 通过扩展支持更多可用的配置项。

想要知道扩展在这的含义，我们先看默认中的实体是怎么配置的。

### 3.1 默认配置

默认情况下，配置一个实体的时候，只能使用  **mybatis-mapper/mapper**( [gitee](https://gitee.com/mybatis-mapper/mapper)
| [GitHub](https://github.com/mybatis-mapper/mapper) ) 中提供的 `@Entity` 注解：
```java
/**
 * 表对应的实体
 *
 * @author liuzh
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Entity {
  /**
   * 对应实体类
   */
  Class<?> value();

  /**
   * 表名
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @interface Table {
    /**
     * 表名，默认空时使用对象名（不进行任何转换）
     */
    String value() default "";
  }

  /**
   * 列名
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  @interface Column {
    /**
     * 列名，默认空时使用字段名（不进行任何转换）
     */
    String value() default "";

    /**
     * 标记字段是否为主键字段
     */
    boolean id() default false;
  }
}
```
在这个注解中嵌套了类型（表）和字段上的两个注解，表上的注解只能设置映射的表名，字段上的注解可以设置字段是否为主键，以及字段对应的列名。
下面是一个使用上述注解的示例：
```java
@Entity.Table("role")
public class Role {
  @Entity.Column(id = true)
  private Long   id;
  @Entity.Column
  private String name;

  //省略setter和getter方法
}
```

默认只提供了最简单的配置信息，可以适用于80%的情况。如果想要设置字段的 `jdbcType` 或者 MyBatis 中的 `TypeHandler`就不行了。
为了能简单的通过扩展支持更多的配置，**mybatis-mapper/mapper**( [gitee](https://gitee.com/mybatis-mapper/mapper)
| [GitHub](https://github.com/mybatis-mapper/mapper) ) 中支持通过 SPI 进行扩展。当前模块 **extend** 就是一个示例。

### 3.2 扩展配置

这里先看扩展后的功能怎么使用，最后在看是如何实现扩展的。

为了扩展更多的配置，在 extend 中新增了下面的注解：
```java
/**
 * 扩展注解，优先级高于默认 {@link io.mybatis.provider.Entity} 注解
 *
 * @author liuzh
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Extend {

  /**
   * 表附加信息
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @interface Table {

    /**
     * 表名，默认空时使用对象名（不进行任何转换）
     */
    String value() default "";

    /**
     * 备注，仅用于在注解上展示，不用于任何其他处理
     */
    String remark() default "";

    /**
     * 使用指定的 <resultMap>
     */
    String resultMap() default "";

    /**
     * 自动根据字段生成 <resultMap>
     */
    boolean autoResultMap() default false;
  }

  /**
   * 列附加信息
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  @interface Column {

    /**
     * 列名，默认空时使用字段名（不进行任何转换）
     */
    String value() default "";

    /**
     * 备注，仅用于在注解上展示，不用于任何其他处理
     */
    String remark() default "";

    /**
     * 标记字段是否为主键字段
     */
    boolean id() default false;

    /**
     * 排序方式，默认空时不作为排序字段，只有手动设置 ASC 和 DESC 才有效
     */
    String orderBy() default "";

    /**
     * 可查询
     */
    boolean selectable() default true;

    /**
     * 可插入
     */
    boolean insertable() default true;

    /**
     * 可更新
     */
    boolean updatable() default true;

    /**
     * 数据库类型 {, jdbcType=VARCHAR}
     */
    JdbcType jdbcType() default JdbcType.UNDEFINED;

    /**
     * 类型处理器 {, typeHandler=XXTypeHandler}
     */
    Class<? extends TypeHandler> typeHandler() default UnknownTypeHandler.class;

    /**
     * 小数位数 {, numericScale=2}
     */
    String numericScale() default "";
  }

}
```

形式和默认的 `@Entity` 很像，这里仍然在其中定义了 `Table` 和 `Column` 注解，在原来基础上增加了更多的属性配置。

#### 3.2.1 `@Extend.Table` 注解

这个注解除了可以设置表名外，还增加了下面几个属性：

- remark：备注，仅用于在注解上展示，不用于任何其他处理
- resultMap：使用指定的 `<resultMap>`
- autoResultMap：自动根据字段生成 `<resultMap>`

备注只是展示，没有真正的作用。真正有用的是后两个字段，通过 `resultMap` 可以指定在其他地方定义好的 `<resultMap>`，
可以直接在 XML 中定义好，这里直接使用。`autoResultMap` 和 `resultMap` 相反，但功能相同，
这个配置会自动根据当前字段的配置生成 `<resultMap>`，所有查询方法都会使用生成的 `<resultMap>`，
这样就能支持查询结果中的 `jdbcType` 和 `TypeHandler` 等配置的应用。

这部分的示例如下：
```java
//autoResultMap 自动生成 <resultMap> 结果映射，支持查询结果中的 typeHandler 等配置
@Extend.Table(value = "sys_user", remark = "系统用户", autoResultMap = true)
public class User {
//忽略其他
}
```

#### 3.2.2 `@Extend.Column` 注解

列的注解增加了大量的属性，这些属性涉及到了查询列、插入列、更新列，以及所有可能出现在 `<result>` 和 `#{}` 中的参数。
这些参数的含义直接看代码注释。

下面是一个完整的示例：
```java
//autoResultMap 自动生成 <resultMap> 结果映射，支持查询结果中的 typeHandler 等配置
@Extend.Table(value = "sys_user", remark = "系统用户", autoResultMap = true)
public class User {
  @Extend.Column(id = true, remark = "主键", updatable = false, insertable = false)
  private Long id;
  @Extend.Column(value = "name", remark = "帐号")
  private String name;
  @Extend.Column(value = "is_admin", remark = "是否为管理员", updatable = false)
  private boolean admin;
  @Extend.Column(remark = "顺序号", orderBy = "DESC")
  private Integer seq;
  @Extend.Column(numericScale = "4", remark = "积分（保留4位小数）")
  private Double points;
  @Extend.Column(selectable = false, remark = "密码")
  private String password;
  @Entity.Column("when_created")
  @Extend.Column(remark = "创建时间", jdbcType = JdbcType.TIMESTAMP)
  private Date whenCreated;
  @Extend.Column(remark = "介绍", typeHandler = StringTypeHandler.class)
  private String info;
  //不是表字段
  private String noExtendColumn;
  //省略其他
}
```
示例中在 `whenCreated` 字段上还混用了 `@Entity.Column` 和 `@Extend.Column`，这里只是说明能混用，
实际上 `@Extend.Column` 提供了所有配置（优先级更高），使用这一个就足够了。

### 3.3 扩展是如何实现的？

>如果不打算扩展，只是使用，可以忽略这部分内容，这部分内容的理解不影响使用。

为什么 `@Extend` 中增加的注解能起到作用，这里涉及到两个问题：

1. 如何实现扩展？
2. 扩展如何起作用的？

首先扩展是通过 SPI 方式实现的，在当前模块的 resources 目录下面，存在 `META-INF/services` 目录，这个目录下面有两个文件：

- io.mybatis.provider.EntityClassFinder
- io.mybatis.provider.EntityFactory

文件名就是接口名，这两个文件中分别是对应的接口实现：

- io.mybatis.provider.extend.ExtendEntityClassFinder
- io.mybatis.provider.extend.ExtendEntityFactory

>更详细的内容等 provider 项目补充文档，这里简单介绍。

#### 3.3.1 EntityClassFinder 接口

先看第一个接口，这个接口的目的是根据下面的方法得到接口对应的实体类类型。
```java
/**
* 查找当前方法对应的实体类
*
* @param mapperType   Mapper 接口，不能为空
* @param mapperMethod Mapper 接口方法，可以为空
* @return
*/
static Optional<Class<?>> find(Class<?> mapperType, Method mapperMethod) {
Objects.requireNonNull(mapperType);
return EntityClassFinderInstance.getInstance().findEntityClass(mapperType, mapperMethod);
}
```

在当前的 `ExtendEntityClassFinder` 中实现如下：
```java
/**
 * 支持识别带有 @Extend.Table 的实体类
 *
 * @author liuzh
 */
public class ExtendEntityClassFinder extends DefaultEntityClassFinder {

  @Override
  public boolean isEntityClass(Class<?> clazz) {
    return clazz.isAnnotationPresent(Extend.Table.class) || super.isEntityClass(clazz);
  }

}
```
这里的意思是除了默认的（`@Entity.Table`）注解外，当类标记 `@Entend.Table` 注解时，也会认为给定的类型是实体类类型。

从这儿也发现，默认情况下，必须给实体类标记注解，否则不会被当做实体类！！！

>目前要求所有实体类必须标记注解，所有属于表中字段的也要标记注解，否则会忽略实体类类型或字段。

#### 3.3.2 EntityFactory 接口

这个接口用于创建 `EntityTable`（实体类信息）和组装创建实体类中的 `EntityColumn`（实体类字段信息）。
创建的这两种类型都使用了装饰者模式，因此会在原有基础上包装一层进行装饰，当前类提供的两个装饰类如下：

- `ExtendEntityTable` 扩展实体类信息，在原有基础上增加了 resultMap 相关的两个功能，而且在查询列、新增列、更新列返回时根据字段进行过滤：
   ```java
   @Override
   public List<EntityColumn> selectColumns() {
     return delegate.selectColumns().stream()
         .filter(column -> column.delegate(ExtendEntityColumn.class).get().selectable())
         .collect(Collectors.toList());
   }

   @Override
   public List<EntityColumn> insertColumns() {
     return delegate.insertColumns().stream()
         .filter(column -> column.delegate(ExtendEntityColumn.class).get().insertable())
         .collect(Collectors.toList());
   }

   @Override
   public List<EntityColumn> updateColumns() {
     return delegate.updateColumns().stream()
         .filter(column -> column.delegate(ExtendEntityColumn.class).get().updatable())
         .collect(Collectors.toList());
   }

   @Override
   public Optional<List<EntityColumn>> orderByColumns() {
     Optional<List<EntityColumn>> orderByColumnsOptional = delegate.orderByColumns();
     List<EntityColumn> orderByColumns = orderByColumnsOptional.orElseGet(ArrayList::new);
     delegate.columns().stream().filter(column -> ((ExtendEntityColumn) column).orderBy() != null
         && !((ExtendEntityColumn) column).orderBy().isEmpty()
         && !orderByColumns.contains(column)).forEach(orderByColumns::add);
     if (orderByColumns.size() > 0) {
       return Optional.of(orderByColumns);
     }
     return Optional.empty();
   }

   @Override
   public Optional<String> orderByColumnList() {
     Optional<List<EntityColumn>> orderByColumns = orderByColumns();
     return orderByColumns.map(entityColumns -> entityColumns.stream()
         .map(column -> column.column() + " " + ((ExtendEntityColumn) column).orderBy())
         .collect(Collectors.joining(",")));
   }
   ```
   这些方法能起作用，是因为 Provider 实现中使用了这些方法，比如 updateByPrimaryKey 方法的实现：
   ```java
   public static String updateByPrimaryKey(ProviderContext providerContext) {
     return SqlScript.caching(providerContext, new SqlScript() {
       @Override
       public String getSql(EntityTable entity) {
         return "UPDATE " + entity.table()
             + " SET " + entity.updateColumns().stream().map(EntityColumn::columnEqualsProperty).collect(Collectors.joining(","))
             + where(() -> entity.idColumns().stream().map(EntityColumn::columnEqualsProperty).collect(Collectors.joining(" AND ")));
       }
     });
   }
   ```
   在这个方法，`SET` 时，调用的 `entity.updateColumns()` 方法，因此在这里设置的 `updatable` 属性会起作用，其他几项也是相同的方式。

- `ExtendEntityColumn` 扩展实体列信息，记录了注解中所有的配置，通过装饰者模式也装饰了大量的方式，举几个方法的例子：
   ```java
   @Override
   public String variables(String prefix) {
     return "#{" + prefix + property()
         + jdbcType().orElse("")
         + typeHandler().orElse("")
         + numericScale().orElse("") + "}";
   }

   /**
    * 数据库类型 {, jdbcType=VARCHAR}
    */
   protected Optional<String> jdbcType() {
     if (this.jdbcType != null && this.jdbcType != JdbcType.UNDEFINED) {
       return Optional.of(", jdbcType=" + jdbcType);
     }
     return Optional.empty();
   }
   ```
   默认的 `variables` 只是返回 `"#{xx}"`，这里会额外增加 `jdbcType`, `typeHandler`，`numericScale` 配置。
   除此之外就是上面 `ExtendEntityTable` 中通过 `selectable,insertable,updatable` 过滤字段。

通过装饰者模式让新增的属性起到了真正的作用。

在看 `ExtendEntityFactory` 实现：
```java
public class ExtendEntityFactory extends EntityFactory {
  public static final int ORDER = DEFAULT_ORDER + 10;

  @Override
  public EntityTable createEntityTable(Class<?> entityClass) {
    return new ExtendEntityTable(next().createEntityTable(entityClass));
  }

  @Override
  public void assembleEntityColumns(EntityTable entityTable) {
    next().assembleEntityColumns(entityTable);
  }

  @Override
  public Optional<List<EntityColumn>> createEntityColumn(EntityField field) {
    Optional<List<EntityColumn>> optionalEntityColumns = next().createEntityColumn(field);
    if (optionalEntityColumns.isPresent()) {
      return optionalEntityColumns.map(columns -> columns.stream().map(ExtendEntityColumn::new).collect(Collectors.toList()));
    } else if (field.isAnnotationPresent(Extend.Column.class)) {
      Extend.Column column = field.getAnnotation(Extend.Column.class);
      String columnName = column.value();
      if (columnName.isEmpty()) {
        columnName = field.getName();
      }
      return Optional.of(Arrays.asList(new ExtendEntityColumn(new EntityColumn(field, columnName, column.id()))));
    }
    return Optional.empty();
  }

  @Override
  public int getOrder() {
    return ORDER;
  }

}
```
`EntityFactory` 是一个责任链模式，通过 `next()` 可以执行下一个方法，
在第一个 `createEntityTable` 方法中，通过 `next().createEntityTable(entityClass)` 调用下一个方法生成 `EntityTable`，
然后自己这里通过 `new ExtendEntityTable(EntityTable delegate)` 创建了装饰类，最终得到的 `EntityTable`
是一个被装饰了多层的对象。

在第二个 `assembleEntityColumns` 方法中，给 `EntityTable` 组装包含的列信息，之所以单独设置这样一个操作，
就是为了让 `EntityTable` 和 `EntityColumn` 都经过所有的处理器进行装饰（如果连在一起，会直接和未被装饰的类产生直接关联）。

第三个 `createEntityColumn` 方法就是根据实体类的字段创建 `EntityColumn`，这里也是责任链和装饰者模式的结合，
默认的 if 中首先支持`@Entity.Column` 注解标记返回的信息，如果没有，就判断是否提供了 `@Extend.Column` 注解。
这个方法入参是一个 `EntityField`（对实体类字段进行了封装，为了方便扩展，例如可以支持方法上的注解（目前不支持）），
但是返回值是 `Optional<List<EntityColumn>>`，一般返回的都是一个 `EntityColumn`，但是当你想要实现嵌套对象时，
这里可以返回多个列的信息（当前不提供该功能）。


通过上面的简单介绍，结合 extend 和 provider 的代码来看，应该能理解，对于直接使用的用户来说，
对这部分内容的理解不影响使用，如果你不打算扩展什么，可以直接忽略这部分内容。