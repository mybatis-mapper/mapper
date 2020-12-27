## 4. common 模块

当前模块包含了简单的封装代码，这部分的代码在 service 和 activerecord 中被用到，
如果不使用这两个包的功能，也不需要 common 模块的代码。

### 4.1 core 包

包含了 Code 响应码（错误码）定义，以及 Controller 中可以用到的 `Response` 类。

默认包含的部分 Code 如下：
```java
public class Code {
  public static final Code SUCCESS        = new Code("200", "操作成功");
  public static final Code FAILURE        = new Code("400", "操作失败");
  public static final Code UNKONWN        = new Code("500", "服务器未知错误");
  public static final Code SAVE_FAILURE   = new Code("501", "保存失败");
  public static final Code UPDATE_FAILURE = new Code("502", "修改失败");
  public static final Code DELETE_FAILURE = new Code("503", "删除失败");
  //忽略其他
}
```

`Response<T>` 是推荐给 `Controller` 使用的，可以直接用，也可以参考自己实现，
这个类主要规范了返回给前端的数据结构：
```java
/**
 * 响应结果
 *
 * @param <T> 数据类型
 */
public class Response<T> {
  /**
   * 单个数据对象
   */
  private T       data;
  /**
   * 数据集合
   */
  private List<T> rows;
  /**
   * 总数，分页查询时的总条数
   */
  private Long    total;
  /**
   * 响应码
   */
  private String  code;
  /**
   * 响应信息
   */
  private String  message;
  
  //忽略其他
}
```
比如返回单个对象时使用 `data` 字段，返回列表时使用 `rows` 字段，分页的总数使用 `total`，
响应码和消息用 `code` 和 `message` 字段。

### 4.2 exception 包

基础的业务异常定义 `ServiceException`:
```java
/**
 * 业务异常
 *
 * @author liuzh
 */
public class ServiceException extends RuntimeException {
  private Code code;

  public ServiceException(Code code) {
    super(code.getMessage());
    this.code = code;
  }

  public ServiceException(Code code, Throwable cause) {
    this(code);
    this.code = code;
  }

  public Code getCode() {
    return code;
  }

  public void setCode(Code code) {
    this.code = code;
  }
}
```
这个异常直接继承的 `RuntimeException`，代码中不需要 `try catch` 包装。

对数据校验产生的断言异常 `AssertException`（继承自 `ServiceException`）：
```java
/**
 * 断言异常
 *
 * @author liuzh
 */
public class AssertException extends ServiceException {
  public static final Code ASSERT_FAILURE = new Code("410", "校验失败");

  public AssertException(String message) {
    super(new Code(ASSERT_FAILURE.getCode(), message));
  }

  public AssertException(Code code) {
    super(code);
  }

}
```

这个异常类，在 util 中的 `Assert` 中被大量使用。

### 4.3 util 包

当前提供了两个工具类。

在 `Utils` 中，主要是各种类型判断空和非空的静态方法，后续根据需要可能会继续增加方法。

在 `Assert` 中，是一些断言的方法，例如下面两对四个方法：
```java
/**
 * 断言是否为真
 *
 * @param expression 布尔值
 * @param message    异常消息
 */
public static void isTrue(boolean expression, String message) {
  if (!expression) {
    throw new AssertException(message);
  }
}

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

/**
 * 断言是否为假
 *
 * @param expression 布尔值
 * @param message    异常消息
 */
public static void isFalse(boolean expression, String message) {
  if (expression) {
    throw new AssertException(message);
  }
}

/**
 * 断言是否为假
 *
 * @param expression 布尔值
 * @param code       错误码
 */
public static void isFalse(boolean expression, Code code) {
  if (expression) {
    throw new AssertException(code);
  }
}
```
这里面用到了 `AssertException` 异常和 `Code` 响应码。

在 service 和 activerecord 中用到了这里的断言，例如：
```java
@Override
public T save(T entity) {
  Assert.isTrue(baseMapper.insert(entity) == 1, SAVE_FAILURE);
  return entity;
}
```
默认的保存方法中，如果保存失败，就抛出失败的异常。如果你的逻辑和这里有区别，可以参考现有代码实现一套自己的核心代码。