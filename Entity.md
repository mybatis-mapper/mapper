## 3. `@Entity` 注解

这部分属于 **mybatis-mapper/provider** 核心部分提供的基础注解，可以直接配合 mapper 使用。

### 3.1 注解方法介绍

注解提供了大量的配置属性，详细介绍看代码注释：

```java
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

    /**
     * 属性配置
     */
    Prop[] props() default {};
  }

  /**
   * 属性配置
   */
  @interface Prop {
    /**
     * 属性名
     */
    String name();

    /**
     * 属性值
     */
    String value();

    /**
     * 属性值类型，支持 String, Integer, Long, Boolean, Double, Float 六种类型
     */
    Class type() default String.class;
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

    /**
     * 属性配置
     */
    Prop[] props() default {};
  }
}
```

### 3.2 `@Entity.Table` 注解

这个注解除了可以设置表名外，还有下面几个属性：

- remark：备注，仅用于在注解上展示，不用于任何其他处理
- resultMap：使用指定的 `<resultMap>`
- autoResultMap：自动根据字段生成 `<resultMap>`
- props: 属性配置，没有明确的作用，实体类上的配置信息会写入 `EntityTable.props` 中

备注只是展示，没有真正的作用。真正有用的是后两个字段，通过 `resultMap` 可以指定在其他地方定义好的 `<resultMap>`，
可以直接在 XML 中定义好，这里直接使用。`autoResultMap` 和 `resultMap` 相反，但功能相同，
这个配置会自动根据当前字段的配置生成 `<resultMap>`，所有查询方法都会使用生成的 `<resultMap>`，
这样就能支持查询结果中的 `jdbcType` 和 `TypeHandler` 等配置的应用。

这部分的示例如下：
```java
//autoResultMap 自动生成 <resultMap> 结果映射，支持查询结果中的 typeHandler 等配置
@Entity.Table(value = "sys_user", remark = "系统用户", autoResultMap = true,
  props = {
    //deleteByExample方法中的Example条件不能为空，默认允许空，另外两个配置类似
    @Entity.Prop(name = "deleteByExample.allowEmpty", value = "false", type = Boolean.class),
    @Entity.Prop(name = "updateByExample.allowEmpty", value = "false", type = Boolean.class),
    @Entity.Prop(name = "updateByExampleSelective.allowEmpty", value = "false", type = Boolean.class)
  })
public class User {
//忽略其他
}
```

### 3.3 `@Entity.Column` 注解

列的注解包含了大量的属性，这些属性涉及到了查询列、插入列、更新列，以及所有可能出现在 `<result>` 和 `#{}` 中的参数。
这些参数的含义直接看代码注释。

下面是一个示例：
```java
//autoResultMap 自动生成 <resultMap> 结果映射，支持查询结果中的 typeHandler 等配置
@Entity.Table(value = "sys_user", remark = "系统用户", autoResultMap = true)
public class User {
  @Entity.Column(id = true, remark = "主键", updatable = false, insertable = false)
  private Long id;
  @Entity.Column(value = "name", remark = "帐号")
  private String name;
  @Entity.Column(value = "is_admin", remark = "是否为管理员", updatable = false)
  private boolean admin;
  @Entity.Column(remark = "顺序号", orderBy = "DESC")
  private Integer seq;
  @Entity.Column(numericScale = "4", remark = "积分（保留4位小数）")
  private Double points;
  @Entity.Column(selectable = false, remark = "密码")
  private String password;
  @Entity.Column(value = "when_created", remark = "创建时间", jdbcType = JdbcType.TIMESTAMP)
  private Date whenCreated;
  @Entity.Column(remark = "介绍", typeHandler = StringTypeHandler.class)
  private String info;
  //不是表字段
  private String noEntityColumn;
  //省略其他
}
```
