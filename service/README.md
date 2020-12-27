## 5. service 模块

service 模块对应开发中的 service 层，或者说是 Spring 中的 `@Service` 标记的类。

这一层基于 mapper 模块中的 `Mapper` 接口，封装了大量开箱即用的 service 层方法。

在设计 service 接口时，考虑了微服务或 RPC 场景下的使用，因此像 `save` 方法就进行了特殊的定义：
```java
/**
 * 保存（所有字段）
 *
 * @param entity 实体类
 * @return 返回保存成功后的实体，远程服务调用时，由于序列化和反序列化，入参和返回值不是同一个对象
 */
T save(T entity);
```

方法注释中也有说明，最常见的场景中，调用 `save` 方法时传递的 `entity` 参数是个引用对象，
`save` 实现中对 `entity` 的修改都会直接影响到调用的传入的引用对象，这种情况下的方法返回值可以直接是 `void`.

但是微服务中跨服务调用时，入参不是引用传递，而是通过序列化传递，服务端对参数的处理不会影响调用端，
调用端想要得到修改后的对象，必须让服务端返回才行，服务端返回时也是先序列化，客户端反序列化，
此时返回的结果和参数不是同一个对象，为了让接口方法更通用，就需要 `save` 方法返回修改后的实体。

另外对于 service 方法操作数据库方法时，错误由 service 层直接判断，比如保存失败时，不是返回0，
而是直接抛出异常：
```java
@Override
public T save(T entity) {
  Assert.isTrue(baseMapper.insert(entity) == 1, SAVE_FAILURE);
  return entity;
}
```
在上面代码中，通过断言（来自 common 包）判断 insert 是否成功，如果失败则抛出保存失败的异常：
```java
/**
 * 断言是否为真
 *
 * @param expression 布尔值
 * @param code       错误码
 */
public static void isTrue(boolean expression, Code code) {
  if (!expression) {
    throw new AssertException(code);
  }
}
```
`SAVE_FAILURE`的定义：
```java
public static final Code SAVE_FAILURE   = new Code("501", "保存失败");
```

下面详细介绍所有接口类的作用和方法的说明。   

### 5.1 `EntityService`

源码中的注释更详细，这里简单把接口列举一下：

- `T save(T entity);`: 保存（所有字段）
- `T saveSelective(T entity);`: 保存（非空字段，空的字段会使用数据库设置的默认值，但是不会字段反写）
- `T update(T entity);`: 更新（所有字段）
- `T updateSelective(T entity);`: 更新（非空字段）
- `T updateSelective(T entity, Fn<T, Object>... forceUpdateFields);`: 更新（非空字段），指定的强制更新字段不区分是否为空
- `boolean pkHasValue(T entity);`: 主键是否有值，**用于判断对象更新或保存，建议重写为更高效的实现**
- `T saveOrUpdate(T entity);`: 保存或更新（全部字段），当主键不存在时保存，存在时更新
- `T saveOrUpdateSelective(T entity);`: 保存或更新（非空字段），当主键不存在时保存，存在时更新
- `int delete(T entity);`: 根据当前类的值作为条件进行删除（注意：当所有字段都没有值时可能会清库）
- `int deleteById(I id);`: 根据主键进行删除
- `<F> int deleteByFieldList(Fn<T, F> field, List<F> fieldValueList);`: 根据指定字段集合删除
- `Optional<T> findById(I id);`: 根据指定的主键查询
- `Optional<T> findOne(T entity);`: 以当前类作为条件查询一个，当结果多于1个时出错
- `List<T> findList(T entity);`: 以当前类作为条件查询
- `<F> List<T> findByFieldList(Fn<T, F> field, List<F> fieldValueList);`: 根据指定字段集合查询
- `List<T> findAll();`: 查询全部
- `long count(T entity);`: 以当前类作为条件查询总数

service 层的方法对 Mapper 方法又进行了一层简单包装，像 `T updateSelective(T entity, Fn<T, Object>... forceUpdateFields);`，
这个方法比 Mapper 中下面的方法更容易理解：
```java
int updateByPrimaryKeySelectiveWithForceFields(@Param("entity") T entity, @Param("fns") Fn.Fns<T> forceUpdateFields);
```

还有一个特殊的 `boolean pkHasValue(T entity);` 方法，**这个方法用于判断对象更新或保存（saveOrUpdate），建议重写为更高效的实现**，例如重新为：
```java
@Override
public boolean pkHasValue(User entity) {
  return user.getId() != null;
}
```
这里写成了固定的 `User`，如果有一个统一的基类，用基类封装更高。

### 5.2 `ExampleService`

源码中的注释更详细，这里简单把接口列举一下：

- `default Example<T> example()`：获取 Example 对象
- `int delete(Example<T> example);`：根据 example 条件批量删除
- `int update(T entity, Example<T> example);`：根据 example 查询条件批量更新（所有字段）
- `int updateSelective(T entity, Example<T> example);`：根据 example 查询条件批量更新（非空字段）
- `Optional<T> findOne(Example<T> example);`：根据 example 条件查询一个，当结果多于1个时出错
- `List<T> findList(Example<T> example);`：根据 example 条件查询
- `long count(Example<T> example);`：根据 example 查询总数

特殊的只有一个方法，这个方法就是为了更方便的得到一个 `Example<T>` 的实例，除此之外，你也可以自己 `new`，例如：
```java
Example<User> example = new Example<>();
```

### 5.3 `BaseService`

`BaseService` 中没有增加任何直接的方法，直接继承了上面两个接口：
```java
/**
 * 基础接口，包含实体类基本接口和 Example 接口
 * <p>
 * 自己的接口不一定要实现这个接口，直接继承会暴露过多的接口，
 * 可以直接在实现类中继承 AbstractService 实现，对外暴露的接口在自己接口中定义，
 * 自己定义的接口和 AbstractService 实现方法的定义一样时，不需要提供实现方法
 *
 * @param <T> 实体类类型
 * @param <I> 主键类型
 * @author liuzh
 */
public interface BaseService<T, I extends Serializable> extends EntityService<T, I>, ExampleService<T, I> {

}
```

这里讨论一个设计上的决策。

当你只需要用到 `EntityService` 中的方法时你可以只继承 `EntityService`，同理 `ExampleService`，
当你两者都需要时，直接 `BaseService`。如果你只有单体应用的开发经验，在自己接口直接继承这些接口时，没有什么不妥的地方。
当你有过微服务开发经验时，你应该发现不妥，微服务中的接口都是对外暴露的服务，如果不加思考的继承了这些 service 接口，
你在无意中就对外暴露了大量的接口方法，这些方法的粒度太细，封装程度太低，极易出现各种方法的随便调用，极易产生分布式事务。

所以，在自己接口选择继承通用接口时，先看看自己是否需要暴露这么多的方法。

虽然这里提供了 3 个接口，但不是不假思索就使用的，如果不继承这些接口，这些接口还有什么用呢？

继续看下面的抽象类。

### 5.4 `AbstractService`

`AbstractService` 抽象类继承了 `BaseService`，实现了所有的接口方法，这个类是推荐拿来直接去用的，
你可以让自己的实现类继承这个抽象类和自己的接口，当想对外暴露 `BaseService` 现成方法时，只需要在自己接口中，
按照相同的参数、返回值、方法名定义一个相同的方法即可，在实现类继承 `AbstractService` 的情况下，
如果不需要重新该接口方法，你的实现类中就不需要写任何代码。例如下面的用法：
```java
public interface RoleService {

  /**
   * 保存（所有字段）
   *
   * @param entity 实体类
   * @return 返回保存成功后的实体，远程服务调用时，由于序列化和反序列化，入参和返回值不是同一个对象
   */
  Role save(Role entity);

  /**
   * 更新（所有字段）
   *
   * @param entity 实体类
   * @return 返回更新成功后的实体，远程服务调用时，由于序列化和反序列化，入参和返回值不是同一个对象
   */
  Role update(Role entity);

  /**
   * 更新（非空字段），指定的强制更新字段不区分是否为空
   *
   * @param entity            实体类
   * @param forceUpdateFields 强制更新的字段，不区分字段是否为 null
   * @return 返回更新成功后的实体，远程服务调用时，由于序列化和反序列化，入参和返回值不是同一个对象
   */
  Role updateSelective(Role entity, Fn<Role, Object>... forceUpdateFields);

  /**
   * 根据主键进行删除
   *
   * @param id 指定的主键
   * @return 返回 1成功，0失败抛出异常
   */
  int deleteById(Integer id);

  /**
   * 根据指定字段集合删除
   *
   * @param field          字段
   * @param fieldValueList 字段值集合
   * @param <F>            字段值类型
   * @return 删除数据的条数
   */
  <F> int deleteByFieldList(Fn<Role, F> field, List<F> fieldValueList);

  /**
   * 根据指定的主键查询
   *
   * @param id 主键
   * @return 实体
   */
  Optional<Role> findById(Integer id);

}
```
上面定义了一个角色的接口，没有继承前面的 3 个接口，只提供了基本的增删改查方法。下面是对应的实现类：
```java
@Service
public class RoleServiceImpl extends AbstractService<Role, Integer, RoleMapper> implements RoleService {

}
```
实现类中没有任何代码，除非你需要修改已有方法的逻辑，否则就不需要任何代码。

以上面例子来简单说明方法定义的规则，在 `AbstractService` 中定义的保存方法如下：
```java
@Override
public T save(T entity) {
  Assert.isTrue(baseMapper.insert(entity) == 1, SAVE_FAILURE);
  return entity;
}
```
在 `RoleService` 中对应的方法定义如下：
```java
/**
 * 保存（所有字段）
 *
 * @param entity 实体类
 * @return 返回保存成功后的实体，远程服务调用时，由于序列化和反序列化，入参和返回值不是同一个对象
 */
Role save(Role entity);
```
因为在继承 `AbstractService<Role, Integer, RoleMapper>` 时指定了泛型的类型，因此这里的定义在抽象类中是存在对应实现的，
因此就不需要自己实现。
