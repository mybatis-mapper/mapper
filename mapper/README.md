## 2. mapper 模块

基于 **mybatis-mapper/provider** 核心部分实现的基础的增删改查操作，提供了一个核心的 `io.mybatis.mapper.Mapper` 接口，接口定义如下：
```java
/**
 * 基础 Mapper 方法，可以在此基础上继承覆盖已有方法
 *
 * @param <T> 实体类类型
 * @param <I> 主键类型
 * @author liuzh
 */
public interface Mapper<T, I extends Serializable>
    extends EntityMapper<T, I>, ExampleMapper<T, Example<T>>, CursorMapper<T, Example<T>> {

  /**
   * 保存实体，默认主键自增，并且名称为 id
   * <p>
   * 这个方法是个示例，你可以在自己的接口中使用相同的方式覆盖父接口中的配置
   *
   * @param entity 实体类
   * @return 1成功，0失败
   */
  @Override
  @Lang(Caching.class)
  @Options(useGeneratedKeys = true, keyProperty = "id")
  @InsertProvider(type = EntityProvider.class, method = "insert")
  int insert(T entity);

  /**
   * 保存实体中不为空的字段，默认主键自增，并且名称为 id
   * <p>
   * 这个方法是个示例，你可以在自己的接口中使用相同的方式覆盖父接口中的配置
   *
   * @param entity 实体类
   * @return 1成功，0失败
   */
  @Override
  @Lang(Caching.class)
  @Options(useGeneratedKeys = true, keyProperty = "id")
  @InsertProvider(type = EntityProvider.class, method = "insertSelective")
  int insertSelective(T entity);

  /**
   * 根据主键更新实体中不为空的字段，强制字段不区分是否 null，都更新
   * <p>
   * 当前方法来自 {@link io.mybatis.mapper.fn.FnMapper}，该接口中的其他方法用 {@link ExampleMapper} 也能实现
   *
   * @param entity            实体类
   * @param forceUpdateFields 强制更新的字段，不区分字段是否为 null，通过 {@link Fn#of(Fn...)} 创建 {@link Fn.Fns}
   * @return 1成功，0失败
   */
  @Lang(Caching.class)
  @UpdateProvider(type = FnProvider.class, method = "updateByPrimaryKeySelectiveWithForceFields")
  int updateByPrimaryKeySelectiveWithForceFields(@Param("entity") T entity, @Param("fns") Fn.Fns<T> forceUpdateFields);

  /**
   * 根据指定字段集合查询：field in (fieldValueList)
   * <p>
   * 这个方法是个示例，你也可以使用 Java8 的默认方法实现一些通用方法
   *
   * @param field          字段
   * @param fieldValueList 字段值集合
   * @param <F>            字段类型
   * @return 实体列表
   */
  default <F> List<T> selectByFieldList(Fn<T, F> field, List<F> fieldValueList) {
    Example<T> example = new Example<>();
    example.createCriteria().andIn((Fn<T, Object>) field, fieldValueList);
    return selectByExample(example);
  }

  /**
   * 根据指定字段集合删除：field in (fieldValueList)
   * <p>
   * 这个方法是个示例，你也可以使用 Java8 的默认方法实现一些通用方法
   *
   * @param field          字段
   * @param fieldValueList 字段值集合
   * @param <F>            字段类型
   * @return 实体列表
   */
  default <F> int deleteByFieldList(Fn<T, F> field, List<F> fieldValueList) {
    Example<T> example = new Example<>();
    example.createCriteria().andIn((Fn<T, Object>) field, fieldValueList);
    return deleteByExample(example);
  }

}
```
这个接口展示了好几个通用方法的特点：
1. 可以继承其他通用接口
2. 可以重写继承接口的定义
3. 可以直接复制其他接口中的通用方法定义
4. 可以使用 Java8 默认方法灵活实现通用方法

在下面内容中，还能看到一个特点，“5. 那就是一个 provider 实现，通过修改接口方法的返回值和入参，就能变身无数个通用方法”，通用方法的实现极其容易。

下面开始详细介绍这些特性。

### 2.1 继承其他通用接口

上面接口定义中，继承了 `EntityMapper`, `ExampleMapper` 和 `CursorMapper` 接口。这些接口中定义了大量的通用方法，
通过继承使得 `Mapper` 接口获得了大量的通用方法，通过继承可以组合不同类别的方法。

除了这里继承的 3 个接口外，还有其他几个接口，一并简单介绍如下。

#### 2.1.1  `io.mybatis.mapper.base.EntityMapper<T, I>`

实体类最基础的通用方法：

- `int insert(T entity)`: 保存实体
- `int insertSelective(T entity)`: 保存实体中不为空的字段
- `int deleteByPrimaryKey(I id)`: 根据主键删除
- `int delete(T entity)`: 保存实体信息批量删除
- `int updateByPrimaryKey(T entity)`: 根据主键更新实体
- `int updateByPrimaryKeySelective(T entity)`: 根据主键更新实体中不为空的字段
- `Optional<T> selectByPrimaryKey(I id)`: 根据主键查询实体
- `Optional<T> selectOne(T entity)`: 根据实体字段条件查询唯一的实体
- `List<T> selectList(T entity)`: 根据实体字段条件批量查询
- `int selectCount(T entity)`: 根据实体字段条件查询总数

这个接口中返回值特殊的有两个 `Optional<T>`，使用的 Java8 中的 `Optional` 类型，表明接口返回值可能为空，
使用时应该判断或使用 `Optional` 提供的 `orXX` 方法，比如下面几种情况：

1. 返回值不能为空，为空则抛出异常：
   ```java
   Optional<User> userOptional = entityMapper.selectByPrimaryKey(user.getId());
   return userOptional.orElseThrow(() -> new RuntimeException("数据不存在"));
   ```
2. 如果不存在，新建一个：
   ```java
   Optional<User> userOptional = entityMapper.selectByPrimaryKey(user.getId());
   return userOptional.orElseGet(User::new)
   ```
3. 如果不存在，直接返回 null：
   ```java
   Optional<User> userOptional = entityMapper.selectByPrimaryKey(user.getId());
   return userOptional.orElse(null);
   ```

真正使用 `Optional` 的时候，要尽可能避免以前 `obj != null` 这种判断思维，避免使用 `Optional.isPresent()` 判断，才能真正掌握 `Optional` 的用法。

如果你不喜欢上面返回值为 `Optional`，就想返回 `T`，后面第 2.5 会介绍实现方式。

#### 2.1.2 `io.mybatis.mapper.example.ExampleMapper<T, E>`

Example 类相关的查询方法，Example 中扩展了一些新的方法，因此默认的 Example 查询已经不支持 MBG 生成的 XXExample 对象作为参数。

先看接口中包含的方法：

- `default Example<T> example()`: 返回 `Example<T>` 对象，自己也可以直接 new
- `int deleteByExample(E example)`: 根据 `Example` 删除
- `int updateByExample(@Param("entity") T entity, @Param("example") E example)`: 根据 `Example` 条件批量更新实体信息
- `int updateByExampleSelective(@Param("entity") T entity, @Param("example") E example);`: 根据 `Example` 条件批量更新实体不为空的字段
- `List<T> selectByExample(E example)`: 根据 `Example` 条件批量查询
- `Optional<T> selectOneByExample(E example)`: 根据 `Example` 条件查询单个实体
- `int countByExample(E example)`: 根据 `Example` 条件查询总数

接口源码中还注释了几个方法，这些方式是用来说明 **2.4** 的特点，这里先不介绍。

看完接口方法，在看看 `Example` 对象中增加了那些特殊的字段和方法：

```java
public class Example<T> {
  /**
   * 排序字段
   */
  protected String            orderByClause;
  /**
   * 是否使用 distinct
   */
  protected boolean           distinct;
  /**
   * 指定查询列
   */
  protected String            selectColumns;
  /**
   * 起始 SQL，添加到 SQL 前，注意防止 SQL 注入
   */
  protected String            startSql;
  /**
   * 结尾 SQL，添加到 SQL 最后，注意防止 SQL 注入
   */
  protected String            endSql;
  /**
   * 多组条件通过 OR 连接
   */
  protected List<Criteria<T>> oredCriteria;
  /**
   * 允许Example查询条件为空
   */
  protected boolean           allowCriteriaEmpty;
  //省略其他
}
```

##### 2.1.2.1 orderByClause

排序字段是默认就有的，但是为了字段使用的安全，增加了额外的赋值方法：
```java
/**
 * 通过方法引用方式设置排序字段
 *
 * @param fn    排序列的方法引用
 * @param order 排序方式
 * @return Example
 */
public Example<T> orderBy(Fn<T, Object> fn, Order order) {
  if (orderByClause == null) {
    orderByClause = "";
  }
  orderByClause += fn.toColumn() + " " + order;
  return this;
}
```
示例：
```java
example.orderBy(User::getId, Example.Order.DESC);
```
这会生成 `ORDER BY id desc` 排序。

##### 2.1.2.2 distinct

去重 distinct 也是默认的。如果设置为 `true`，查询时就会使用 `SELECT DISTINCT ...`。

##### 2.1.2.3 selectColumns

`selectColumns` 是新增的，可以用来指定查询列，对应的方法如下：
```java
/**
 * 指定查询列
 *
 * @param fns 方法引用
 */
public Example<T> selectColumns(Fn<T, Object>... fns) {
  if (selectColumns == null) {
    selectColumns = "";
  }
  for (Fn<T, Object> fn : fns) {
    String column = fn.toColumn();
    String field = fn.toField();
    if (selectColumns.length() != 0) {
      selectColumns += ",";
    }
    if (column.equals(field)) {
      selectColumns += column;
    } else {
      selectColumns += column + " AS " + field;
    }
  }
  return this;
}
```
用法示例：
```Examplejava
<User> example = exampleMapper.example();
example.selectColumns(User::getUserName, User::getSex);
//可以多次调用追加查询列
example.selectColumns(User::getId);
```
指定查询列后，在 SQL 中会变成 `select name as userName, sex, id from ...`（`userName` 映射的 `name`）。

##### 2.1.2.4 startSql

新增加的字段，起始 SQL，添加到 SQL 前，**注意防止 SQL 注入。**

直接通过 set 方法设置值：
```java
/**
 * 设置起始 SQL
 *
 * @param startSql 起始 SQL，添加到 SQL 前，注意防止 SQL 注入
 */
public void setStartSql(String startSql) {
  this.startSql = startSql;
}
```
这个字段基本上要配合下面的 `endSql` 字段一起用，下面示例：
```java
example.setStartSql("select * from (");
example.setEndSql(") tmp limit 1");
```
示例没太大意义，你可以想个更好的例子来更新此处文档。

##### 2.1.2.5 endSql

和上面新增加的字段类似：结尾 SQL，添加到 SQL 最后，注意防止 SQL 注入

也是直接通过 set 方法设置：
```java
/**
 * 设置结尾 SQL
 *
 * @param endSql 结尾 SQL，添加到 SQL 最后，注意防止 SQL 注入
 */
public void setEndSql(String endSql) {
  this.endSql = endSql;
}
```
示例看前一个字段的。

##### 2.1.2.6 oredCriteria

默认就有的字段，记录查询条件块。

##### 2.1.2.7 allowCriteriaEmpty

这个字段是新增的，这个字段破坏了 Example 的整体结构，主要目的是在使用时决定：是否允许查询条件为空

当 select 查询时，是可以为空的，这种情况就是查询了全表，由于这个字段默认值为 `false` 不允许，因此你需要 `new Example(true)` 才行。

当 delete, update 时，默认不允许为空，这可以避免清库和更新全库。这个参数只在下面方法用到：
```java
/**
 * 获取所有条件，当前方法会校验所有查询条件，如果不存在查询条件就抛出异常。
 * <p>
 * 不允许通过 Example 相关方法操作全表！！！
 *
 * @return 条件
 */
public List<Criteria<T>> getOredCriteria() {
  if (!allowCriteriaEmpty) {
    if (oredCriteria.size() == 0) {
      throw new IllegalArgumentException("Example 条件不能为空");
    }
    boolean noCriteria = true;
    for (Criteria<T> criteria : oredCriteria) {
      if (!criteria.getCriteria().isEmpty()) {
        noCriteria = false;
        break;
      }
    }
    if (noCriteria) {
      throw new IllegalArgumentException("Example 条件不能为空");
    }
  }
  return oredCriteria;
}
```
当获取查询条件时判断，如果没有有效的查询条件，就抛出异常，避免潜在的风险。

##### 2.1.2.7 `clear()` 方法

最后说一个方法，同一个 `Example` 对象经过 `clear()` 后是可以反复使用的：
```java
/**
 * 清除所有设置
 */
public void clear() {
  oredCriteria.clear();
  orderByClause = null;
  distinct = false;
  selectColumns = null;
  startSql = null;
  endSql = null;
  allowCriteriaEmpty = false;
}
```
特别注意的是 `allowCriteriaEmpty`，即使你用 `Example(true)` 创建的，这里也会设置为 `false`，想要设置 `true` 可以调用下面方法：
```java
/**
 * 设置是否允许查询条件为空
 *
 * @param allowCriteriaEmpty true允许空，一般用于查询，false不允许空，一般用于修改和删除
 */
public void allowCriteriaEmpty(boolean allowCriteriaEmpty) {
  this.allowCriteriaEmpty = allowCriteriaEmpty;
}
```

##### 2.1.2.8 简单示例
介绍完所有字段和一个特殊方法，下面看几个 `Example` 示例：
```java
Example<User> example = new Example();
example.createCriteria().andGreaterThan(User::getId, 10L).andLike(User::getUserName, "殷%");
Assert.assertEquals(3, exampleMapper.deleteByExample(example));

example.clear();
example.createCriteria().andEqualTo(User::getId, 1L);
User user = new User();
user.setId(1L);
user.setUserName("男主角");
Assert.assertEquals(1, exampleMapper.updateByExample(user, example));
```

剩下的，你需要掌握的就是 `andGreaterThan` 和 `andEqualTo` 这类的方法，这些方法通过名字可以直接理解。

#### 2.1.3 `io.mybatis.mapper.cursor.CursorMapper<T, E>`

游标相关的功能，是在 2016年初的 MyBatis 3.3.1 中增加的，当你想逐个从数据库取值时可以使用。

通用方法中，仅提供了两个方法示例：

- `Cursor<T> selectCursor(T entity)`: 根据实体字段条件查询
- `Cursor<T> selectCursorByExample(E example)`：根据 Example 条件查询

这两个方法就提现了 “5. 那就是一个 provider 实现，通过修改接口方法的返回值和入参，就能变身无数个通用方法”，
这两个接口使用了 `EntityProvider` 和 `ExampleProvider` 中的现成实现，只是把返回值从 `List<T>` 改成了 `Cursor<T>`。

一个简单的示例如下：
```java
@Test
public void testSelectCursor() {
  SqlSession sqlSession = getSqlSession();
  try {
    CursorMapper<User, Example<User>> mapper = sqlSession.getMapper(UserMapper.class);
    User user = new User();
    user.setSex("女");
    Cursor<User> userCursor = mapper.selectCursor(user);
    Iterator<User> userIterator = userCursor.iterator();
    int count = 0;
    while (userIterator.hasNext()) {
      count++;
      User u = userIterator.next();
      System.out.println(u.getUserName());
      Assert.assertEquals(count, userCursor.getCurrentIndex() + 1);
    }
    Assert.assertEquals(16, count);
    Assert.assertTrue(userCursor.isConsumed());
  } finally {
    //不要忘记关闭sqlSession
    sqlSession.close();
  }
}
```

#### 2.1.4 `io.mybatis.mapper.fn.FnMapper<T>`

这个接口主要目的是为了演示如果通过传入字段改变查询的逻辑。接口包含下面几个方法：

```java
public interface FnMapper<T> {

  /**
   * 根据主键更新实体中不为空的字段，强制字段不区分是否 null，都更新
   *
   * @param entity            实体类
   * @param forceUpdateFields 强制更新的字段，不区分字段是否为 null，通过 {@link Fn#of(Fn...)} 创建 {@link Fn.Fns}
   * @return 1成功，0失败
   */
  @Lang(Caching.class)
  @UpdateProvider(type = FnProvider.class, method = "updateByPrimaryKeySelectiveWithForceFields")
  int updateByPrimaryKeySelectiveWithForceFields(@Param("entity") T entity, @Param("fns") Fn.Fns<T> forceUpdateFields);

  /**
   * 根据实体字段条件查询唯一的实体（{@link io.mybatis.mapper.example.ExampleMapper} 可以实现一样的功能，当前方法只是示例）
   *
   * @param entity       实体类
   * @param selectFileds 查询的字段，不区分字段是否为 null，通过 {@link Fn#of(Fn...)} 创建 {@link Fn.Fns}
   * @return 单个实体，查询结果由多条时报错
   */
  @Lang(Caching.class)
  @SelectProvider(type = FnProvider.class, method = "selectColumns")
  Optional<T> selectColumnsOne(@Param("entity") T entity, @Param("fns") Fn.Fns<T> selectFileds);

  /**
   * 根据实体字段条件批量查询（{@link io.mybatis.mapper.example.ExampleMapper} 可以实现一样的功能，当前方法只是示例）
   *
   * @param entity       实体类
   * @param selectFileds 查询的字段，不区分字段是否为 null，通过 {@link Fn#of(Fn...)} 创建 {@link Fn.Fns}
   * @return 实体列表
   */
  @Lang(Caching.class)
  @SelectProvider(type = FnProvider.class, method = "selectColumns")
  List<T> selectColumns(@Param("entity") T entity, @Param("fns") Fn.Fns<T> selectFileds);

}
```
>第一个方法名有点长，用列表展示不太合适了。

这几个方法，都可以通过传入字段改变行为，并且第一个名字最长的方法，直接被复制到了 `Mapper` 接口的定义中，这就是第 3 点要说明的内容，
实际上你可以复制粘贴的方式构造一个自己的基类 Mapper。2.3 在介绍。

名字最长这个方法是许多人想要的一个方法，在选择更新的情况下还能指定必须更新的字段，这个方法的示例如下：
```java
User user = mapper.selectByPrimaryKey(1L).get();
user.setUserName(null);
int count = mapper.updateByPrimaryKeySelectiveWithForceFields(user, Fn.of(User::getUserName));
```
单纯从 Mapper 接口调用来看，这个方法名字太长，写法也有点别扭，等看到后续 Service 层封装时，
会掩盖这些不舒服的地方，会变得更简洁，例如 Service 中的接口定义：
```java
/**
 * 更新（非空字段），指定的强制更新字段不区分是否为空
 *
 * @param entity            实体类
 * @param forceUpdateFields 强制更新的字段，不区分字段是否为 null
 * @return 返回更新成功后的实体，远程服务调用时，由于序列化和反序列化，入参和返回值不是同一个对象
 */
T updateSelective(T entity, Fn<T, Object>... forceUpdateFields);
```
调用的示例：
```java
userService.updateSelective(user, User::getName, User::getRoleId);
```

#### 2.1.5 `io.mybatis.mapper.list.ListMapper<T>`

这个接口算示例，没有包含在 `Mapper` 中，提供下面一个方法：

- `int insertList(@Param("entityList") List<? extends T> entityList);`: 批量插入

需要数据库支持 `INSERT TABLE(C1,C2...) VALUES(...), (....)` 语法才可以。

### 2.2 重写继承接口的定义

在 `EntityMapper` 中有 `insert` 方法定义如下：
```java
/**
 * 保存实体
 *
 * @param entity 实体类
 * @return 1成功，0失败
 */
@Lang(Caching.class)
@InsertProvider(type = EntityProvider.class, method = "insert")
int insert(T entity);
```
这个定义没有处理主键，需要自己设置好主键后调用该方法新增数据。

如果我使用的 MySql 自增怎么办？主键null也能直接保存，但是不回写。

如果使用 Oracle 序列怎么办？直接用这个方法是没有办法的。

因为可以 **重写继承接口的定义**，所以可以支持所有 MyBatis 本身能支持的所有主键方式。

在 `Mapper` 中，覆盖定义如下：
```java
/**
 * 保存实体，默认主键自增，并且名称为 id
 * <p>
 * 这个方法是个示例，你可以在自己的接口中使用相同的方式覆盖父接口中的配置
 *
 * @param entity 实体类
 * @return 1成功，0失败
 */
@Override
@Lang(Caching.class)
@Options(useGeneratedKeys = true, keyProperty = "id")
@InsertProvider(type = EntityProvider.class, method = "insert")
int insert(T entity);
```
首先 `@Override` 是重写父接口定义，然后和原来相比增加了下面的注解：
```java
@Options(useGeneratedKeys = true, keyProperty = "id")
```
这个注解对应 xml 中的配置如下：
```xml
<insert id="insert" useGeneratedKeys="true" keyProperty="id">
```
`useGeneratedKeys` 意思是要用JDBC接口方式取回主键，主键字段对应的属性名为 `id`，就是要回写到 `id` 字段。

上面的配置对 MySQL 这类自增数据库是可行的，如果你自己的主键不叫 `id`，甚至如果每个表的主键都不统一（如 `{tableName}_id`），
你需要在每个具体实现的接口中重写。例如：
```java
public interface UserMapper extends Mapper<User, Long> {
  /**
   * 保存实体，默认主键自增，并且名称为 id
   * <p>
   * 这个方法是个示例，你可以在自己的接口中使用相同的方式覆盖父接口中的配置
   *
   * @param entity 实体类
   * @return 1成功，0失败
   */
  @Override
  @Lang(Caching.class)
  @Options(useGeneratedKeys = true, keyProperty = "userId")
  @InsertProvider(type = EntityProvider.class, method = "insert")
  int insert(User entity);

}
```

如果是Oracle序列或者需要执行SQL生成主键或者取回主键时，可以配置 `@SelectKey` 注解，示例如下：
```java
@Override
@Lang(Caching.class)
@SelectKey(statement = "CALL IDENTITY()", keyProperty = "id", resultType = Long.class, before = false)
@InsertProvider(type = EntityProvider.class, method = "insert")
int insert(User entity);
```

上面还只是通过增加注解重新定义了接口方法。实际上你还可以更换 `@InsertProvider(type = EntityProvider.class, method = "insert")`，
将其中的实现换成其他的也可以，如果对默认的方法和逻辑不满意，就可以改成别的。

通过 **重写继承接口的定义**，应该能感觉出有多强大，多么灵活。

### 2.3 复制其他接口中的通用方法定义

这是最灵活的一点，在 `Mapper` 中直接复制了 `FnMapper` 的一个方法：
```java
/**
 * 根据主键更新实体中不为空的字段，强制字段不区分是否 null，都更新
 * <p>
 * 当前方法来自 {@link io.mybatis.mapper.fn.FnMapper}，该接口中的其他方法用 {@link ExampleMapper} 也能实现
 *
 * @param entity            实体类
 * @param forceUpdateFields 强制更新的字段，不区分字段是否为 null，通过 {@link Fn#of(Fn...)} 创建 {@link Fn.Fns}
 * @return 1成功，0失败
 */
@Lang(Caching.class)
@UpdateProvider(type = FnProvider.class, method = "updateByPrimaryKeySelectiveWithForceFields")
int updateByPrimaryKeySelectiveWithForceFields(@Param("entity") T entity, @Param("fns") Fn.Fns<T> forceUpdateFields);
```
这就是完全的复制粘贴，利用这一点，你可以不用 `Mapper` 接口作为自己的基类接口，你可以定义一个自己的接口，复制粘贴自己的需要的通用方法作为基础接口，
例如一个 `GuozilanMapper` 示例如下：
```java
public interface GuozilanMapper<T> {
  
  /**
   * 保存实体
   *
   * @param entity 实体类
   * @return 1成功，0失败
   */
  @Lang(Caching.class)
  @InsertProvider(type = EntityProvider.class, method = "insert")
  int insert(T entity);

  /**
   * 根据主键查询实体
   *
   * @param id 主键
   * @return 实体
   */
  @Lang(Caching.class)
  @SelectProvider(type = EntityProvider.class, method = "selectByPrimaryKey")
  Optional<T> selectByPrimaryKey(Long id);
}
```

只要继承了上面的接口，你就直接拥有了这两个基础方法。

> 使用这种方式可以自定义一些自己项目需要用到的不同类别的通用接口，例如，如果你有大量实体都没有主键，默认的 `Mapper<T, I>` 就不太适合，
> 此时你可以自己创建一个 `NoIdMapper<T>`，把除了主键操作方法外的其他方法（有选择的）都拷过来，就形成了符合自己实际需要的通用 Mapper。

推而广之之后，还有更绝的用法，不继承接口，或者基础接口没有某个方法，直接复制注解过来，不需要自己写 XML：

```java
public interface UserMapper {

   /**
    * 保存实体
    *
    * @param entity 实体类
    * @return 1成功，0失败
   */
  @Lang(Caching.class)
  @InsertProvider(type = EntityProvider.class, method = "insert")
  int insert(User entity);
}
```
你不需要任何具体的 SQL，上面的 insert 方法就可以直接使用了。

>后期长远规划中，会利用 3.5.6 中的一些功能，直接配置一个 `@InsertProvider` 就能使用，不需要配置 `type` 和 `method`。
> 在更长远的规划中，可以直接支持 JPA 中的方法名查询，例如
> `getByNameEqualAndAgeLessThen(@Param("name") String name, @Param("age") Integer age)`
> 直接根据方法名生成一个 SQL。

### 2.4 使用 Java8 默认方法灵活实现通用方法

在 `Mapper` 接口中，利用现有的 `Example` 方法，实现了两个非常常用的通用方法：
```java
/**
 * 根据指定字段集合查询：field in (fieldValueList)
 * <p>
 * 这个方法是个示例，你也可以使用 Java8 的默认方法实现一些通用方法
 *
 * @param field          字段
 * @param fieldValueList 字段值集合
 * @param <F>            字段类型
 * @return 实体列表
 */
default <F> List<T> selectByFieldList(Fn<T, F> field, List<F> fieldValueList) {
  Example<T> example = new Example<>();
  example.createCriteria().andIn((Fn<T, Object>) field, fieldValueList);
  return selectByExample(example);
}

/**
 * 根据指定字段集合删除：field in (fieldValueList)
 * <p>
 * 这个方法是个示例，你也可以使用 Java8 的默认方法实现一些通用方法
 *
 * @param field          字段
 * @param fieldValueList 字段值集合
 * @param <F>            字段类型
 * @return 实体列表
 */
default <F> int deleteByFieldList(Fn<T, F> field, List<F> fieldValueList) {
  Example<T> example = new Example<>();
  example.createCriteria().andIn((Fn<T, Object>) field, fieldValueList);
  return deleteByExample(example);
}
```
这两个方法可以直接根据某个字段值的集合进行批量查询或者删除，用法示例如下：
```java
List<User> users = mapper.selectByFieldList(User::getUserName, Arrays.asList("张无忌", "赵敏", "周芷若"));
mapper.deleteByFieldList(User::getUserName, Arrays.asList("张无忌", "赵敏", "周芷若"));
```

除了这个例子外，还有一段 `EntityMapper` 被注释的示例：
```java
/**
 * 根据实体字段条件分页查询
 *
 * @param entity    实体类
 * @param rowBounds 分页信息
 * @return 实体列表
 *//*
List<T> selectList(T entity, RowBounds rowBounds);

*//**
 * 根据查询条件获取第一个结果
 *
 * @param entity 实体类
 * @return 实体
 *//*
default Optional<T> selectFirst(T entity) {
  List<T> entities = selectList(entity, new RowBounds(0, 1));
  if (entities.size() == 1) {
    return Optional.of(entities.get(0));
  }
  return Optional.empty();
}

*//**
 * 根据查询条件获取指定的前几个对象
 *
 * @param entity 实体类
 * @param n      指定的个数
 * @return 实体
 *//*
default List<T> selectTopN(T entity, int n) {
  return selectList(entity, new RowBounds(0, n));
}
```
合理的通过 Java8 的默认方法，能够实现海量的通用方法。至于那些是真正需要用到的通用方法，就需要根据自己的需要来选择，因此虽然上面的方法能通用，
但是在缺乏频繁使用场景的情况下，`Mapper` 接口并没有接纳这几个方法。

### 2.5 通过修改接口方法的返回值和入参，就能变身无数个通用方法

以 `EntityProvider` 中的 `select` 方法为例，方法的具体实现如下：
```java
/**
 * 根据实体字段条件查询唯一的实体，根据实体字段条件批量查询，查询结果的数量由方法定义
 *
 * @param providerContext 上下文
 * @return cacheKey
 */
public static String select(ProviderContext providerContext) {
  return SqlScript.caching(providerContext, new SqlScript() {
    @Override
    public String getSql(EntityTable entity) {
      return "SELECT " + entity.baseColumnAsPropertyList()
          + " FROM " + entity.table()
          + ifParameterNotNull(() ->
          where(() ->
              entity.whereColumns().stream().map(column ->
                  ifTest(column.notNullTest(), () -> "AND " + column.columnEqualsProperty())
              ).collect(Collectors.joining(LF)))
      )
          + entity.groupByColumn().orElse("")
          + entity.havingColumn().orElse("")
          + entity.orderByColumn().orElse("");
    }
  });
}
```
最终会生成一个 `SELECT .. FROM .. WHERE ...` 的 SQL，在 MyBatis 中，SQL 只定义了如何在数据库执行，
执行后的结果和取值方式是通过接口方法定义决定的，因此就这样一个 SELECT 查询，能够实现很多个方法，举例如下：
```java
@Lang(Caching.class)
@SelectProvider(type = EntityProvider.class, method = "select")
Optional<T> selectOne(T entity);

@Lang(Caching.class)
@SelectProvider(type = EntityProvider.class, method = "select")
List<T> selectList(T entity);

@Lang(Caching.class)
@SelectProvider(type = EntityProvider.class, method = "select")
List<T> selectAll();

@Lang(Caching.class)
@SelectProvider(type = EntityProvider.class, method = "select")
Cursor<T> selectCursor(T entity); 
```

利用这一特点，通过修改接口方法的返回值和入参，就能变身无数个通用方法。

>如果在加个 `RowBounds` 分页参数，直接翻倍。
