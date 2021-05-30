## 6. activerecord 模块

>**[Active Record](https://zh.wikipedia.org/wiki/Active_Record)** 模式
> 
>在软件工程中，主动记录模式（active record pattern）是一种架构模式，
> 可见于在关系数据库中存储内存中对象的软件中。
> 它在Martin Fowler的2003年著《企业应用架构的模式》书中命名。
> 符合这个模式的对象的接口将包括函数比如插入、更新和删除，
> 加上直接对应于在底层数据库表格中列的或多或少的属性。
>
>主动记录模式是访问在数据库中的数据的一种方式。数据库表或视图被包装入类。
> 因此，对象实例被链接到这个表格的一个单一行。
> 在一个对象创建之后，在保存时将一个新行增加到表格中。
> 加载的任何对象都从数据库得到它的信息。在一个对象被更新的时候，
> 在表格中对应的行也被更新。包装类为在表格或视图中的每个列都实现访问器方法或属性。

activerecord 模块提供了 4 个接口类、1个工具类、1个配置类和1个Spring Boot自动配置类。

下面分别说明每个类的方法和设计的目的。

### 6.1 MapperRecord

这个接口定义如下：
```java
public interface MapperRecord<T, I extends Serializable, M extends Mapper<T, I>> extends EntityInfoMapper<T> {

  /**
   * 通用 Mapper
   *
   * @return 通用 Mapper
   */
  default M baseMapper() {
    return MapperProvider.<T, I, M>getDefaultInstance().baseMapper(entityClass());
  }

}
```
可以看到这个接口还继承了一个 `EntityInfoMapper<T>` 接口，继承的这个接口可以根据泛型 `<T>` 获取 `T` 对应的具体的实体类类型。
也就是这里默认方法中的 `entityClass()` 方法，当前接口中定义的这个默认方法用于返回实体类对应的 `Mapper` 接口，
获取接口的方式为通过 `MapperProvider.<T, I, M>getDefaultInstance()` 得到一个默认实现，然后根据当前实体类类型，
从默认实现中得到一个 `Mapper` 实例。

在实体类中得到自己对应的 `Mapper` 后就能调用各种通用的方法，下面要看的两个接口就是对该默认方法 `baseMapper()` 的应用。

### 6.2 `EntityRecord`

`EntityRecord` 接口中包含了实体类的基础 CRUD 方法，接口中全是默认方法，
实体类继承就可以非常直接进行增删改查操作，下面是方法列表和介绍：

- `default void save()`：保存（所有字段）
- `default void saveSelective()`：保存（非空字段，空的字段会使用数据库设置的默认值，但是不会字段反写）
- `default void update()`：根据主键更新（所有字段）
- `default void updateSelective()`：根据主键更新（非空字段）
- `default void updateSelective(Fn<T, Object>... forceUpdateFields)`：根据主键更新（非空字段），指定的强制更新字段不区分是否为空
- `default I pkValue()`：返回主键值，建议子类替换为效率更高的判断方式（例如主键为 id 的情况下，直接 return id）
- `default boolean pkHasValue()`：主键是否有值
- `default void saveOrUpdate()`：保存或更新（全部字段），当主键不存在时保存，存在时更新
- `default void saveOrUpdateSelective()`：保存或更新（非空字段），当主键不存在时保存，存在时更新
- `default int delete()`：根据当前类的值作为条件进行删除（注意：当所有字段都没有值时可能会清库）
- `default void deleteById()`：根据主键进行删除
- `default void deleteById(I id)`：根据指定的主键进行删除
- `default <F> int deleteByFieldList(Fn<T, F> field, List<F> fieldValueList)`：根据指定字段集合删除
- `default T findById(I id)`：根据指定的主键查询
- `default T findOne()`：以当前类作为条件查询一个，当结果多于1个时出错
- `default List<T> findList()`：以当前类作为条件查询
- `default <F> List<T> findByFieldList(Fn<T, F> field, List<F> fieldValueList)`：根据指定字段集合查询
- `default List<T> findAll()`：查询全部
- `default long count()`：以当前类作为条件查询总数

大部分方法都是以当前实体为参数进行增删改查的操作，少数几个方法可以通过传参进行其他的操作。
实际上除了以自己作为参数的方法外，其他外部参数的方法都应该设为静态方法才合适，
由于当前接口的形式无法实现，只能通过创建一个实例后再进行操作。

### 6.3 `ExampleRecord`

`ExampleRecord` 和上面的接口类似，提供了 `Example` 相关的方法：

- `default Example<T> example()`：获取 Example 对象
- `default int delete(Example<T> example)`：根据 example 条件批量删除
- `default int update(Example<T> example)`：根据 example 查询条件批量更新（所有字段）
- `default int updateSelective(Example<T> example)`：根据 example 查询条件批量更新（非空字段）
- `default T findOne(Example<T> example)`：根据 example 条件查询一个，当结果多于1个时出错
- `default List<T> findList(Example<T> example)`：根据 example 条件查询
- `default long count(Example<T> example)`：根据 example 查询总数

### 6.4 `ActiveRecord`

`ActiveRecord` 没有定义方法，只是单独的把上面两个接口集合到一起：
```java
/**
 * 建议将继承该抽象类的实现类的作用范围控制在 Service 层，不能超出范围，其它层使用时转换为 VO 或 DTO 后使用
 *
 * @param <T> 实体类类型
 * @param <I> 主键类型
 * @author liuzh
 */
public interface ActiveRecord<T, I extends Serializable>
    extends EntityRecord<T, I>, ExampleRecord<T, I> {

}
```

重点看一下上面的注释，说一说 ActiveRecord 模式的应用场景。

在一个简单的增删改查系统中，如果用不到 service 层，那就可以直接在 controller 层用实体类进行直接的CRUD。

但是在一个有 service 层的系统中，如果要实现模块的隔离，就需要考虑实体类只能在 service 层使用，
不能让实体类逸出到其他层，因此在和 controller 层交互时就需要有 VO 或 DTO 进行数据转换，
这样才能保证实体类调用 CRUD 方法不出错。

### 6.5 `MapperProvider`

第一个接口中就调用到了当前类的静态方法，当前类中提供了两个静态方法：
```java
/**
 * 获取默认实例
 *
 * @return 默认实例
 */
public static <T, I extends Serializable, M extends Mapper<T, I>> MapperProvider<T, I, M> getDefaultInstance() {
  return MapperProviderInstance.getINSTANCE();
}

/**
 * 获取指定的实例
 *
 * @return 默认实例
 */
public static <T, I extends Serializable, M extends Mapper<T, I>> MapperProvider<T, I, M> getInstance(String instanceName) {
  return (MapperProvider<T, I, M>) applicationContext.getBean(instanceName);
}
```
当系统只有一个数据源时，直接用第一个方法返回默认数据源即可，当系统存在多个数据源时，就需要第二个方法来指定调用哪一个数据源。

当前模块的测试用例就是一个多数据源的，我们看看如何配置的，这里使用了 [Spring XML](./src/test/java/io/mybatis/activerecord/spring/spring.xml) 形式,
主要部分的两个配置如下
```xml
<!-- 通过 init-method 将当前实例作为默认实现 -->
<bean id="mapperProviderRole" class="io.mybatis.activerecord.spring.MapperProvider"
      init-method="registerAsDefault">
  <constructor-arg index="0" ref="sqlSessionTemplateRole"/>
</bean>

<bean id="mapperProviderUser" class="io.mybatis.activerecord.spring.MapperProvider">
  <constructor-arg index="0" ref="sqlSessionTemplateUser"/>
</bean>
```
这里配置了两个 `MapperProvider` 实现，第一个方法在执行了 `registerAsDefault`，这个方法定义如下：
```java
/**
 * 将当前实例设置为默认实例
 */
public void registerAsDefault() {
  MapperProviderInstance.setINSTANCE(this);
}
```
这个方法将自己设为了默认实例。另一个 `mapperProviderUser` 不是默认的，这个对象在下面的基类中使用：
```java
public class BaseId<T extends BaseId> implements ActiveRecord<T, Integer> {
  @Extend.Column(id = true, insertable = false)
  private Integer id;

  @Override
  public Integer pkValue() {
    return id;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @Override
  public Mapper<T, Integer> baseMapper() {
    //获取指定数据源的实例
    return MapperProvider.<T, Integer, Mapper<T, Integer>>getInstance("mapperProviderUser").baseMapper(entityClass());
  }
}
```
这个基类重新了 `baseMapper()` 使用了指定数据源创建的 `mapperProviderUser`。

这个类中还有一个希望实现的最佳实践方法，就是 `pkValue()` 方法，这个方法的默认实现如下：
```java
/**
 * 返回主键值，建议子类替换为效率更高的判断方式（例如主键为 id 的情况下，直接 return id）
 *
 * @return 主键值
 */
default I pkValue() {
  EntityTable entityTable = baseMapper().entityTable();
  List<EntityColumn> idColumns = entityTable.idColumns();
  if (idColumns.size() == 1) {
    return (I) idColumns.get(0).field().get(this);
  } else {
    return idColumns.get(0).field().get(this) != null ? (I) this : null;
  }
}
```
可以看到这里先得到了当前实体对应的主键列，然后通过反射的方式判断主键字段是否有值。

虽然这个方法的执行效率没太大影响，但是如果重新成下面的方法，肯定更高效：
```java
@Override
public Integer pkValue() {
  return id;
}
```
在这里的 Spring 示例中，通过 XML 定义的 `MapperProvider`，当使用 Spring Boot 时，可以自动配置直接使用。

### 6.6 `MapperProviderAutoConfiguration`

`MapperProviderAutoConfiguration` 用来自动配置 `MapperProvider`，配合使用的 `MapperProviderProperties` 目前没太大用途，
只是可以用来配置关闭自动配置。

当应用中没有 `MapperProvider` 实例时，会自动创建一个。当存在一个实例时，还会自动执行实例的 `registerAsDefault()` 方法注册为默认实例。

当 Spring Boot 中也存在多数据源时，可以通过给其中一个实例添加 `@Primary` 来指定首选的默认实例。