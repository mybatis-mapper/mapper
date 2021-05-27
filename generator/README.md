# `睿Rui` - 代码生成器

本项目直接使用了一个功能强大的代码生成器 `睿Rui`，这个代码生成器可以设置项目的目录结构和具体的代码模板，
可以方便的从零生成一个完整的项目，也可以在已有项目结构中生成指定的代码文件。

## 项目文件简介

所有内容都在 `src/main/resources` 下面：
- `lib` jar 目录，将数据库驱动放到该目录下
  - `mysql-connector-java-5.1.49.jar` MySQL驱动，当前只提供了MySQL驱动，
    自己可以替换版本，连接其他数据库时，将对应的驱动放到当前 lib 目录下
  - `rui-cli.jar` 代码生成器 
- `mapper-templates` 模板文件
  - `extend` 基于 `@Extend` 注解的代码模板
    - `model.java`  普通的实体类模板
    - `model-lombok.java` 使用 lombok 注解的实体类模板
  - `simple` 基于 `@Entity` 注解的代码模板
      - `model.java`  普通的实体类模板
      - `model-lombok.java` 使用 lombok 注解的实体类模板
  - `mapper.java` Mapper 接口模板
  - `mapper.xml` Mapper XML 模板
  - `service.java` Service 接口模板
  - `serviceImpl.java` Service 接口实现模板
- `project.yaml` 代码生成器配置文件

## 代码生成器配置 project.yaml

下面通过注释介绍每一项配置的含义或者作用：
```yaml
# 模板的名字，生成代码的根目录名称
name: mybatis-mapper-demo
# 代码生成的路径，可以使用 SYS 和 ENV 等变量，具体包含的内容和运行环境有关
# user.dir 为当前运行命令的目录，path 不设置时的默认值也是当前路径，在命令行中可以通过 —o 重新指定位置
# 当 path 路径以 .zip 后缀结尾时，会将生成的目录结构和代码放到压缩包中
path: ${SYS['user.dir']}/
# 模板文件所在路径，默认为相对 path 所在的位置，不设置时和 path 相同
templates: mapper-templates
# 数据库配置
database: 
  # 数据库连接配置
  jdbcConnection:
    # 使用方言，默认为 JDBC 方式，可能会取不到表或字段注释
    # 后续介绍如何通过字典匹配注释值
    dialect: MYSQL
    # jdbc驱动
    driver: com.mysql.jdbc.Driver
    # jdbc连接地址
    url: jdbc:mysql://localhost:3306/test?useSSL=false
    # 用户名
    user: root
    # 密码
    password: root
  # 获取表配置，支持 % 和 _ 模糊匹配，可以配置多个值  
  # 还有一个支持复杂规则 tableRules 属性后续单独介绍
  tables:
    - '%'
  # 根据类型对字段类型打标签, 可以通过 column.tags.TAG 的 true/false 来判断当前列是否有该标签
  # 主要对列进行分类，方便后续模板中的使用
  typeTags:
    # 示例，针对日期时间设置 jdbcType 标签，生成的代码中，如果有该标签，会指定 jdbcType 的值
    jdbcType:
      - DATE
      - TIME
      - TIMESTAMP
  # 关键字包装，这里的示例针对 MySQL，不提供默认值，需要根据数据库语法进行设置，其中的 %s 为关键字（表名或列名）
  keywordWrap: "`%s`"
  # 数据库关键字，列举的关键字会进行包装处理，没有默认值，需要自己配置所有可能的关键字
  keywords:
    - order
    - desc
# 附加属性
attrs: # 子目录（文件）可以通过 parent.parent...attrs.属性名 可以逐级向上使用属性
  # 这里设置了一个基础包名
  basePackage: io.mybatis.demo.mybatis
# 子目录（文件）
files:
  # 文件名，可以 / 设置多级目录
  - name: src/main
    # 子目录（文件）
    files:
      - name: java
        files: 
          # 包，可以多层，名称可以引用当前结构的所有属性，也可以使用 tables 数据表信息
          - name: '${project.attrs.basePackage}'
            # 设置类型为 package，尽可能都指定，默认会计算，默认为目录。包下面的默认都是包
            type: PACKAGE
            files:
              # 包名
              - name: mapper
                files:
                  # 模板文件，下面 iter 对 tables 所有表进行迭代，因此这里的模板会根据表的数量进行循环生成
                  # 迭代循环时，单个迭代对象的默认名为 it，因此这里的 name 就是表名（类名形式）为前缀的 Mapper 接口
                  - name: '${it.name.className}Mapper.java'
                    # 使用的模板名，会从上面配置的 templates 下面查找
                    # 包含多级目录时，这里可以指定相对 templates 的路径
                    file: mapper.java
                    # 可选配置 iter, 迭代的数据对象，当前可选为 tables 为反射的数据库表信息
                    # iter 值使用 mvel 表达式取值，除 tables 外也可以对当前 yaml 中的集合类型进行迭代，例如 project.files
                    iter: tables
                    # 可选配置 iterName，默认值为 it，在模板和上面的 name 中可以使用改变量的值
                    # iterName: it
                    # 可选配置 filter, 绑定数据过滤器，使用 mvel 表达式，过滤条件不满足时不执行当前的操作，不生成相应的目录或文件
                    # filter: tables.size() > 0 # 仅仅是示例，size=0时本身也不会循环
                    # 可选配置 itFilter，迭代数据过滤器，使用 mvel 表达式，当指定 iter 时，对迭代的数据进行过滤
                    # 不满足条件时，只忽略当前的 it 数据，下面示例中当表名以 sys 为前缀时生成目录或代码
                    # iterFilter: it.name.original.o.startsWith("sys")
                    # 可选配置 enabled，默认 true，当前目录（文件）有效，会生成，设置为 false 时忽略
                    # enabled: true
                    # 可选配置 mode，默认值为 OVERRIDE，生成目录或文件时覆盖，可选值为:
                    # OVERRIDE: 覆盖
                    # ONCE: 只生成一次，或者如果目录或文件已存在时不生成(目录存在时，子文件也不会生成）
                    # MERGE: 合并已存在的文件和新生成的文件，需要满足规则才能合并
                    # mode: OVERRIDE
                    # 可选配置 times，默认值 1，默认情况下，模板只经过一次变量替换，有时存在多层变量嵌套时，执行多次才能全部替换
                    # times: 1
              - name: model
                files:
                  - name: '${it.name.className}.java'
                    # 此处有4种模板示例，分别列出来加了注释
#                    file: simple/model.java
#                    file: simple/model-lombok.java
                    file: extend/model.java
#                    file: extend/model-lombok.java
                    iter: tables
              - name: service
                files:
                  - name: '${it.name.className}Service.java'
                    file: service.java
                    iter: tables
                  - name: impl
                    files:
                      - name: '${it.name.className}ServiceImpl.java'
                        file: serviceImpl.java
                        iter: tables
      - name: resources
        files:
          - name: mappers
            files:
              - name: '${it.name.className}Mapper.xml'
                iter: tables
                file: mapper.xml
```

在模板的 YAML 配置中，数据库相关的配置定义了生成代码的元数据，files 中的内容定义了项目的结构和生成代码的位置。

变化比较多的配置是具体的一个 files 配置，下面是上面大段注释部分的整理，file 属性说明：

- `name: '${it.name.className}Mapper.java'`
  模板文件，下面 iter 对 tables 所有表进行迭代，因此这里的模板会根据表的数量进行循环生成；
  迭代循环时，单个迭代对象的默认名为 `it`，因此这里的 name 就是表名（类名形式）为前缀的 Mapper 接口。
- `file: mapper.java` 使用的模板名，会从上面配置的 templates 下面查找;
  包含多级目录时，这里可以指定相对 templates 的路径。
- `iter: tables`: 可选配置 iter, 迭代的数据对象，当前可选为 tables 为反射的数据库表信息；
  iter 值使用 mvel 表达式取值，除 tables 外也可以对当前 yaml 中的集合类型进行迭代，例如 project.files。
- `iterName: it`
  可选配置 iterName，默认值为 it，在模板和上面的 name 中可以使用改变量的值。
- `filter: tables.size() > 0` 可选配置 filter, 绑定数据过滤器，使用 mvel 表达式，过滤条件不满足时不执行当前的操作，不生成相应的目录或文件。
- `iterFilter: it.name.original.o.startsWith("sys")`
  可选配置 itFilter，迭代数据过滤器，使用 mvel 表达式，当指定 iter 时，对迭代的数据进行过滤；
  不满足条件时，只忽略当前的 it 数据，下面示例中当表名以 sys 为前缀时生成目录或代码。
- `enabled: true` 可选配置 enabled，默认 true，当前目录（文件）有效，会生成，设置为 false 时忽略。
- `mode: OVERRIDE` 可选配置 mode，默认值为 OVERRIDE，生成目录或文件时覆盖，可选值为:
  - `OVERRIDE`: 覆盖，默认值
  - `ONCE`: 只生成一次，或者如果目录或文件已存在时不生成(目录存在时，子文件也不会生成）
  - `MERGE`: 合并已存在的文件和新生成的文件，需要满足规则才能合并
- `times: 1`
  可选配置 times，默认值 1，默认情况下，模板只经过一次变量替换，有时存在多层变量嵌套时，执行多次才能全部替换

>上面示例中的 `iterFilter: it.name.original.o.startsWith("sys")` 中的 `it.name.original.o` 很奇怪，下面模板介绍为什么会这么写。

除了上面这些属性外，还有一个只在示例配置文件出现了一次的类型 `type`：

```yaml
- name: '${project.attrs.basePackage}'
  type: PACKAGE
```

`type`: 文件类型，有下面4种类型：
- `DIR`: 目录
- `PACKAGE`: 代码结构目录（例如：java中的包名）
- `TEMPLATE`: 模板，经过模板赋值处理（只有 `file` 没有 `name` 时，生成的文件名使用 `file` 名，当同时存在时，`name` 为文件名，`file` 为内容）
- `STATIC`: 静态内容，不经过模板处理（只有 `name` 没有 `file` 时认为是 `STATIC`，找不到文件就是空文件）

一般情况下，必须设置第一级包（`PACKAGE`）的类型，也就是示例中第一层的包，`name`可以多层(如`io.mybatis.mapper`)，不必把第一级包名单独列出来设置。

其他没有设置的都是运行时自动计算的类型，计算类型的规则如下：
1. 如果指定了类型，就使用指定的类型（配置的根默认是 `DIR`，根指的是和 `name`,`path`,`templates`一级的属性）。
2. 如果包含 `files` 属性，也就是有子目录/子文件的情况，这种情况下：
  1. 如果存在上级，并且上级为 `PACKAGE` 类型，则当前类型也是 `PACKAGE`，所以 `PACKAGE` 类型下面的目录默认都是 `PACKAGE`。
  2. 如果没有上级或者上级不是 `PACKAGE`，则为 `DIR` 类型。
3. 如果 `file` 属性有值（配置的模板名），为 `TEMPLATE` 类型。
4. 如果 `name` 属性有值（生成的文件名），为 `STATIC` 类型。
5. 如果 `parent` 存在，就和 `parent.type` 一样。
5. 上面情况都不满足时，默认为 `DIR` 类型。

## 模板文件介绍

目前代码模板支持 `FreeMarker` 和 `Mvel2` 两种，默认使用 `FreeMarker` 模板引擎，不能修改为其他模板引擎，后续根据反馈进行扩展。

以 `extend/model.java` 为例，分段对代码模板进行介绍。
```java
package ${package};
```
首先是 `${package}`，所有在 `file.type: PACKAGE` 下面的代码模板都可以直接使用该变量，
这个变量代表了当前模板文件所在包，包名自动计算，
可以通过 `${package.parent}` 获取上级包名，可以递归使用，例如 `${package.parent.parent}`。

```java
import io.mybatis.provider.Entity;
import io.mybatis.provider.extend.Extend;
import org.apache.ibatis.type.JdbcType;

<#list it.importJavaTypes as javaType>
import ${javaType};
</#list>
```
这里是`import`部分，除了根据模板内容写死的部分内容外，还要把数据类型导入进来，数据类型在 `it.importJavaTypes` 中。
当在其他代码引用当前这个 model 类时，可以使用下面的方式导入：
```java
import ${project.attrs.basePackage}.model.${it.name.className};
```
如果知道包的相对位置，也可以:
```java
import ${package.parent}.model.${it.name.className};
```

再之后就是当前 model 的类名和基本的注释信息:
```java
/**
 * ${it.name} - ${it.comment}
 *
 * @author ${SYS['user.name']}
 */
@Extend.Table("${it.name}" remark = "${it.comment}", autoResultMap = true)
public class ${it.name.className} {
```
这里给实体类添加了 mybatis-mapper 中的 `@Extend.Table` 注解，
这里使用 `${SYS['user.name']}` 作为 `@author`，你也可以写死，或者设置为其他变量。

这里的 `${it.name}` 代表了表名，`${it.name.className}` 代表了表名对应的类名形式，
这里的 `it.name` 有很多种形式，假设有表名或字段名为: `user_role`,下面举例列出所有可用的形式。

- `${it.name}`: 表名，值为: `user_role`
- `${it.name.original}`: 原始表名，值为: `user_role`
- `${it.name.lowercase}`: 表名的小写形式，值为: `user_role`
- `${it.name.uppercase}`: 表名的大写形式，值为: `USER_ROLE`
- `${it.name.underlineCase}`: 表名的下划线形式，值为: `user_role`（驼峰会转下划线）
- `${it.name.noUnderlineCase}`: 表名的无下划线形式，值为: `userrole`
- `${it.name.upperUnderlineCase}`: 表名的大写下划线形式，值为: `USER_ROLE`（驼峰会转下划线）
- `${it.name.camelCase}`: 表名的驼峰形式，值为: `userRole`
- `${it.name.upperCamelCase}`: 表名的大写驼峰形式，值为: `UserRole`
- `${it.name.className}`: 表名的类名形式，值为: `UserRole`
- `${it.name.fieldName}`: 表名的字段形式，值为: `userRole`
  
除了上面的多种形式外，除 `${it.name}` 外，其他形式都还有复数形式：

- `${it.name.className.o}`: 表名的类名形式的原始值，和不带 `.o` 一样
- `${it.name.className.s}`: 表名的类名形式的复数形式，值为: `UserRoles`
- `${it.name.fieldName.o}`: 表名的字段形式的原始值，和不带 `.o` 一样
- `${it.name.fieldName.s}`: 表名的字段形式的复数形式，值为: `userRoles`

>复数形式一般用于 RESTFul `Controller` 时的规范资源路径名。

因为 `it.name` 不是简单的 `String` 类型，因此使用最终的原始名称和 `String` 运算时，
需要使用下面的形式：
```
${it.name.original.o}
# 或者 mvel 表达式时
it.name.toString()
```

继续回到 `extend/model.java` 模板：
```java
  <#list it.columns as column>
  <#if column.pk>
  @Extend.Column(value = "${column.name}", id = true, remark = "${column.comment}", updatable = false, insertable = false)
  <#else>
  @Extend.Column(value = "${column.name}", remark = "${column.comment}"<#if column.tags.jdbcType>, jdbcType = JdbcType.${column.jdbcType}</#if>)
  </#if>
  private ${column.javaType} ${column.name.fieldName};
  
  </#list>
  
  <#list it.columns as column>
```
通过 `freemarker` 语法中的 `<#list>` 对 `it` 中的所有列 `columns` 进行循环，
区分主键类型进行分别处理。

在 `<#if column.tags.jdbcType>, jdbcType = JdbcType.${column.jdbcType}</#if>` 中，
用到了代码生成器配置中的标签功能，如果有 `jdbcType` 标签，就在注解指定 `JdbcType` 的类型，
主要是日期读写时，指定类型才能取对日期时间的精确度。

通过 `${column.javaType}` 获取字段类型，这个类型不带包名，因此在 `import` 时导入了字段类型。

也可以写成带有包名的完整类型名： `${column.javaType.fullName}`。

代码模板最后一部分就是 setter 和 getter 方法：
```java
  <#list it.columns as column>
  /**
   * 获取 ${column.comment}
   *
   * @return ${column.name.fieldName} - ${column.comment}
   */
  public ${column.javaType} get${column.name.className}() {
    return ${column.name.fieldName};
  }
  
  /**
   * 设置${column.comment}
   *
   * @param ${column.name.fieldName} ${column.comment}
   */
  public void set${column.name.className}(${column.javaType} ${column.name.fieldName}) {
    this.${column.name.fieldName} = ${column.name.fieldName};
  }
  
  </#list>
}
```
当使用 lombok 版本的模板时，实体类使用了 `@Getter` 和 `@Setter` 注解，代码体中就不需要生成 getter 和 setter 的代码。
如果你想使用 lombok 的 `@Data` 或其他注解，你可以实现自己的模板（注意 `import` 相应的类型）。

## ENV 和 SYS 可选值，使用示例

代码生成器配置和模板中都可以使用 ENV 和 SYS 变量，这些变量和运行的环境有关，
代码生成器运行时会输出当前环境的这些信息，例如下面的内容（示例为 macOS）：
```
[main] TRACE Project - SYS可用参数:
[main] TRACE Project - SYS['gopherProxySet'] = false
[main] TRACE Project - SYS['awt.toolkit'] = sun.lwawt.macosx.LWCToolkit
[main] TRACE Project - SYS['socksProxyHost'] = 127.0.0.1
[main] TRACE Project - SYS['http.proxyHost'] = 127.0.0.1
[main] TRACE Project - SYS['java.specification.version'] = 11
[main] TRACE Project - SYS['sun.cpu.isalist'] = 
[main] TRACE Project - SYS['sun.jnu.encoding'] = UTF-8
[main] TRACE Project - SYS['https.proxyPort'] = 8889
[main] TRACE Project - SYS['java.vm.vendor'] = Oracle Corporation
[main] TRACE Project - SYS['sun.arch.data.model'] = 64
[main] TRACE Project - SYS['idea.test.cyclic.buffer.size'] = 1048576
[main] TRACE Project - SYS['java.vendor.url'] = https://openjdk.java.net/
[main] TRACE Project - SYS['user.timezone'] = 
[main] TRACE Project - SYS['os.name'] = Mac OS X
[main] TRACE Project - SYS['java.vm.specification.version'] = 11
[main] TRACE Project - SYS['sun.java.launcher'] = SUN_STANDARD
[main] TRACE Project - SYS['user.country'] = CN
[main] TRACE Project - SYS['jdk.debug'] = release
[main] TRACE Project - SYS['sun.cpu.endian'] = little
[main] TRACE Project - SYS['user.home'] = /Users/liuzh
[main] TRACE Project - SYS['user.language'] = zh
[main] TRACE Project - SYS['java.specification.vendor'] = Oracle Corporation
[main] TRACE Project - SYS['java.version.date'] = 2020-07-14
[main] TRACE Project - SYS['java.home'] = /Library/Java/JavaVirtualMachines/jdk-11.0.8.jdk/Contents/Home
[main] TRACE Project - SYS['file.separator'] = /
[main] TRACE Project - SYS['https.proxyHost'] = 127.0.0.1
[main] TRACE Project - SYS['java.vm.compressedOopsMode'] = Zero based
[main] TRACE Project - SYS['line.separator'] = 

[main] TRACE Project - SYS['java.specification.name'] = Java Platform API Specification
[main] TRACE Project - SYS['java.vm.specification.vendor'] = Oracle Corporation
[main] TRACE Project - SYS['java.awt.graphicsenv'] = sun.awt.CGraphicsEnvironment
[main] TRACE Project - SYS['user.script'] = Hans
[main] TRACE Project - SYS['sun.management.compiler'] = HotSpot 64-Bit Tiered Compilers
[main] TRACE Project - SYS['java.runtime.version'] = 11.0.8+10-LTS
[main] TRACE Project - SYS['user.name'] = liuzh
[main] TRACE Project - SYS['path.separator'] = :
[main] TRACE Project - SYS['os.version'] = 10.14.6
[main] TRACE Project - SYS['java.runtime.name'] = Java(TM) SE Runtime Environment
[main] TRACE Project - SYS['file.encoding'] = UTF-8
[main] TRACE Project - SYS['java.vm.name'] = Java HotSpot(TM) 64-Bit Server VM
[main] TRACE Project - SYS['java.vendor.version'] = 18.9
[main] TRACE Project - SYS['java.vendor.url.bug'] = https://bugreport.java.com/bugreport/
[main] TRACE Project - SYS['java.io.tmpdir'] = /var/folders/2t/___9446s3_13b0mqw389wsy40000gn/T/
[main] TRACE Project - SYS['java.version'] = 11.0.8
[main] TRACE Project - SYS['user.dir'] = /Users/liuzh/IdeaProjects/rui/core
[main] TRACE Project - SYS['os.arch'] = x86_64
[main] TRACE Project - SYS['socksProxyPort'] = 1080
[main] TRACE Project - SYS['java.vm.specification.name'] = Java Virtual Machine Specification
[main] TRACE Project - SYS['java.awt.printerjob'] = sun.lwawt.macosx.CPrinterJob
[main] TRACE Project - SYS['sun.os.patch.level'] = unknown
[main] TRACE Project - SYS['java.vendor'] = Oracle Corporation
[main] TRACE Project - SYS['java.vm.info'] = mixed mode
[main] TRACE Project - SYS['java.vm.version'] = 11.0.8+10-LTS
[main] TRACE Project - SYS['sun.io.unicode.encoding'] = UnicodeBig
[main] TRACE Project - SYS['java.class.version'] = 55.0
[main] TRACE Project - SYS['http.proxyPort'] = 8889
[main] TRACE Project - ENV可用参数:
[main] TRACE Project - ENV['PATH'] = /usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:/usr/local/go/bin:/usr/local/go/bin/
[main] TRACE Project - ENV['JAVA_MAIN_CLASS_59298'] = com.intellij.rt.junit.JUnitStarter
[main] TRACE Project - ENV['SHELL'] = /bin/bash
[main] TRACE Project - ENV['USER'] = liuzh
[main] TRACE Project - ENV['VERSIONER_PYTHON_PREFER_32_BIT'] = no
[main] TRACE Project - ENV['TMPDIR'] = /var/folders/2t/___9446s3_13b0mqw389wsy40000gn/T/
[main] TRACE Project - ENV['SSH_AUTH_SOCK'] = /private/tmp/com.apple.launchd.ExoXHAM01C/Listeners
[main] TRACE Project - ENV['XPC_FLAGS'] = 0x0
[main] TRACE Project - ENV['VERSIONER_PYTHON_VERSION'] = 2.7
[main] TRACE Project - ENV['__CF_USER_TEXT_ENCODING'] = 0x1F5:0x19:0x34
[main] TRACE Project - ENV['Apple_PubSub_Socket_Render'] = /private/tmp/com.apple.launchd.ka761yBfMO/Render
[main] TRACE Project - ENV['LOGNAME'] = liuzh
[main] TRACE Project - ENV['LC_CTYPE'] = zh_CN.UTF-8
[main] TRACE Project - ENV['PWD'] = /Users/liuzh/IdeaProjects/rui/core
[main] TRACE Project - ENV['XPC_SERVICE_NAME'] = com.jetbrains.intellij.7796.51577403-F31F-4931-B778-60546B7A8CE8
[main] TRACE Project - ENV['TOOLBOX_VERSION'] = 1.20.8352
[main] TRACE Project - ENV['HOME'] = /Users/liuzh
```
在模板中使用时，直接参考上面的写法，例如 `${SYS['user.home']}` 和 `${ENV['HOME']}`。

了解代码生成器配置和模板后，再看如何使用代码生成器生成代码。

## 代码生成器 rui-cli.jar

代码生成器提供了一个可执行 jar 文件: rui-cli.jar

> 后续会提供基于 java swing 的客户端，可以通过 UI 简单操作生成代码。

可执行 jar 文件使用方式：

```
用法: java -cp "lib/*" io.mybatis.rui.cli.Main [options]
  Options:
    -p, --project
      代码生成器YAML配置文件
    -o, --output
      输出目录，默认使用配置文件中的 path，输出目录如果带 .zip 后缀，就会将生成的代码导出为压缩包
    -T, --templates
      模板文件路径，默认和YAML相同位置，或者为当前执行目录的相对位置
    --jdbc.dialect
      数据库方言
      Possible Values: [JDBC, HSQLDB, ORACLE, DB2, SQLSERVER, MARIADB, MYSQL]
    --jdbc.driver
      数据库驱动
    --jdbc.url
      数据库URL
    --jdbc.user
      数据库用户
    --jdbc.password
      数据库密码
    -t, --tables
      要获取的表名，支持模糊匹配(%)，多个表名用逗号隔开，指定该值后会覆盖配置文件中的值
    -A, -attrs
      项目附加属性，会覆盖项目下的 attrs 配置
      Syntax: -Akey=value
      Default: {}
    -h, --help
      显示帮助信息
```

在上面命令中，推荐 `-cp lib/*` 使用 lib 目录下面的所有 jar 包，这样当你需要不同的数据库驱动时，
将 JDBC 驱动放到该目录下面即可。

下面演示代码生成器的使用过程。

## 使用文档

上面介绍了很多代码生成器的细节，最关键的内容是项目结构和代码模板，
这两部分内容需要理解上面的内容后，根据自己的项目进行设计和模板编写，
一旦编写好，只要项目结构和模板内容不变，这套模板就可以一直使用，
后续变化的地方就是 JDBC连接配置，指定要连的数据库和要获取的表信息，
因此除了代码生成器的初始配置和模板有一点难度外，后续的使用是非常简单的。

接下来我们以当前项目提供的模板为基础，介绍一下如果想应用到自己的项目，该如何修改配置，修改完配置之后如何生成项目结构和代码。

### 配置适配

#### 生成代码的目录

在 `project.yaml` 中，`path` 定义了项目所在的路径（不含项目名），
示例中使用了默认值（不配置时也是改值）`${SYS['user.dir']}/`，
这个目录和执行代码生成器命令时所在的目录一样，如果是第一次生成项目，
生成到当前目录也没有问题，如果是已经存在的项目，
就需要修改路径让代码可以直接生成到已有项目的目录结构中。

**假设项目为 d:/idea-project/cms**，cms 是项目名，此时的配置如下：
```yaml
# 项目名，和真实的 cms 名字对应
name: cms
# 项目所在路径
path: d:/idea-project/
```

#### 数据库配置

数据库连接的配置按照示例修改即可，平时使用时，最常见的变化时 `tables` 的值。

如果后续生成代码时不想重新生成已存在表的代码，就需要每次指定要生成的表的信息。

例如第一次的时候生成了所有 `sys_user`, `sys_role` 等 `sys` 开头的表，此时可以配置为：
```yaml
tables:
  - sys%
```

假设后续要生成某几个表的，可以配置为具体的表名：
```yaml
tables:
  - account
  - content
  - comments
```

表名可以写多个，而且每个都可以是具体表名，也可以使用模糊匹配，使用 `%` 匹配任意个字符，使用 `_` 匹配单个字符。

#### 包名修改

示例中为了展示 `attrs` 属性的用法，因此在这里配置了一个 `basePackage` 属性，这个属性在下面的包名 `name` 使用了，
不使用 `attrs`，直接将包名写到 `name` 也可以。按照这里的写法，修改为自己项目的包名，
例如：`com.company.cms`，如果你的包和这里结构不一样，可以自己调整 `files` 适配自己的结构。

#### 类名规则

在 mapper 包下面循环生成了每个表对应的 Mapper 接口，这里定义的名称为 `'${it.name.className}Mapper.java'`，
已表的类名+`Mapper` 后缀作为接口名，如果你喜欢使用 `Dao` 后缀，就可以改为 `'${it.name.className}Dao.java'`，
还需要注意修改所有使用（注入）该接口的地址，例如在 `serviceImpl.java` 模板中：
```java
//省略其他
import ${project.attrs.basePackage}.mapper.${it.name.className}Dao;
//省略其他
@Service
public class  ${it.name.className}ServiceImpl extends AbstractService<${it.name.className}, Long, ${it.name.className}Dao> implements ${it.name.className}Service {

}
```
只要保证模板内容和类型定义保持一致即可。

### 执行代码生成器

配置完成后，使用下面的命令生成代码：
```java
java -cp "lib/*" io.mybatis.rui.cli.Main -p project.yaml
```

>**由于写此文档时使用的 macOS，因此下面的日志文件和这里预设的目录结构不同，后续更新为目录一致的输出**

执行命令后，输出日志如下：
```
[main] TRACE Project - SYS可用参数:
[main] TRACE Project - SYS['gopherProxySet'] = false
#-----------------省略大量 SYS 属性-----------------------
[main] TRACE Project - SYS['http.proxyPort'] = 8889
[main] TRACE Project - ENV可用参数:
[main] TRACE Project - ENV['JAVA_ARCH'] = x86_64
#-----------------省略大量 ENV 属性-----------------------
[main] TRACE Project - ENV['_'] = /usr/bin/java
[main] DEBUG Project - 执行程序路径: /Users/liuzh/IdeaProjects/generator-cmd
[main] DEBUG Project - basedir变量: /Users/liuzh/IdeaProjects/generator-cmd
[main] DEBUG Project - yamlDir变量: /Users/liuzh/IdeaProjects/generator-cmd
[main] DEBUG Database - 获取数据库信息
[2021-05-30 17:40:37] [DEBUG] rui.pJ: Use [Hutool Console Logging] Logger As Default.
[main] DEBUG Database - 获取表: role
[main] TRACE Database - 记录列: id
[main] TRACE Database - 记录列: role_name
[main] DEBUG Database - 获取表: user
[main] TRACE Database - 记录列: id
[main] TRACE Database - 记录列: user_name
[main] TRACE Database - 记录列: user_age
[main] TRACE Database - 记录列: address
[main] DEBUG Database - 获取表: user_role
[main] TRACE Database - 记录列: id
[main] TRACE Database - 记录列: user_id
[main] TRACE Database - 记录列: role_id
[main] DEBUG Database - 通过 SQL 获取表的注释信息
[main] DEBUG Database - 执行 SQL: SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'test' AND TABLE_NAME in ( 'user_role','role','user' )
[main] DEBUG Database - 表: role - 角色
[main] DEBUG Database - 表: user - 用户
[main] DEBUG Database - 表: user_role - 用户和角色关联
[main] DEBUG Context - 生成项目路径: /Users/liuzh/IdeaProjects/
[main] DEBUG Context - 读取模板路径: mapper-templates
[main] DEBUG Generator - 已存在目录: /Users/liuzh/IdeaProjects/cms
[main] DEBUG Generator - 已存在目录: /Users/liuzh/IdeaProjects/cms/src/main
[main] DEBUG Generator - 已存在目录: /Users/liuzh/IdeaProjects/cms/src/main/java
[main] DEBUG Generator - 已存在包: io.mybatis.demo.mybatis
[main] DEBUG Generator - 已存在包: mapper
[main] DEBUG Generator - 初次创建文件: RoleMapper.java
[main] DEBUG Generator - 初次创建文件: UserMapper.java
[main] DEBUG Generator - 初次创建文件: UserRoleMapper.java
[main] DEBUG Generator - 已存在包: model
[main] DEBUG Generator - 初次创建文件: Role.java
[main] DEBUG Generator - 初次创建文件: User.java
[main] DEBUG Generator - 初次创建文件: UserRole.java
[main] DEBUG Generator - 已存在包: service
[main] DEBUG Generator - 初次创建文件: RoleService.java
[main] DEBUG Generator - 初次创建文件: UserService.java
[main] DEBUG Generator - 初次创建文件: UserRoleService.java
[main] DEBUG Generator - 已存在包: impl
[main] DEBUG Generator - 初次创建文件: RoleServiceImpl.java
[main] DEBUG Generator - 初次创建文件: UserServiceImpl.java
[main] DEBUG Generator - 初次创建文件: UserRoleServiceImpl.java
[main] DEBUG Generator - 已存在目录: /Users/liuzh/IdeaProjects/cms/src/main/resources
[main] DEBUG Generator - 已存在目录: /Users/liuzh/IdeaProjects/cms/src/main/resources/mappers
[main] DEBUG Generator - 初次创建文件: RoleMapper.xml
[main] DEBUG Generator - 初次创建文件: UserMapper.xml
[main] DEBUG Generator - 初次创建文件: UserRoleMapper.xml
```

rui-cli.jar 中包含了多个可以覆盖默认配置的参数，大部分参数仍然建议在配置文件中指定。

如果想要生成的代码导出为 ZIP 压缩包，只需要在上面的命令后面增加 `-o cms.zip` 即可在执行的目录中生成一个 `cms.zip` 压缩包文件：
```java
java -cp "lib/*" io.mybatis.rui.cli.Main -p project.yaml -o cms.zip
```
执行命令后，最终生成 `cms.zip` 文件，文件结构如下：
```
$ unzip -v cms.zip 
Archive:  cms.zip
 Length   Method    Size  Cmpr    Date    Time   CRC-32   Name
--------  ------  ------- ---- ---------- ----- --------  ----
       0  Defl:N        2   0% 05-30-2021 18:17 00000000  cms/
       0  Defl:N        2   0% 05-30-2021 18:17 00000000  cms/src/
       0  Defl:N        2   0% 05-30-2021 18:17 00000000  cms/src/main/
       0  Defl:N        2   0% 05-30-2021 18:17 00000000  cms/src/main/java/
       0  Defl:N        2   0% 05-30-2021 18:17 00000000  cms/src/main/java/io/
       0  Defl:N        2   0% 05-30-2021 18:17 00000000  cms/src/main/java/io/mybatis/
       0  Defl:N        2   0% 05-30-2021 18:17 00000000  cms/src/main/java/io/mybatis/demo/
       0  Defl:N        2   0% 05-30-2021 18:17 00000000  cms/src/main/java/io/mybatis/demo/mybatis/
       0  Defl:N        2   0% 05-30-2021 18:17 00000000  cms/src/main/java/io/mybatis/demo/mybatis/mapper/
     177  Defl:N      135  24% 05-30-2021 18:17 bf55dc13  cms/src/main/java/io/mybatis/demo/mybatis/mapper/RoleMapper.java
     177  Defl:N      135  24% 05-30-2021 18:17 46655d51  cms/src/main/java/io/mybatis/demo/mybatis/mapper/UserMapper.java
     205  Defl:N      160  22% 05-30-2021 18:17 b161f496  cms/src/main/java/io/mybatis/demo/mybatis/mapper/UserRoleMapper.java
       0  Defl:N        2   0% 05-30-2021 18:17 00000000  cms/src/main/java/io/mybatis/demo/mybatis/model/
     991  Defl:N      438  56% 05-30-2021 18:17 a7ea4bb7  cms/src/main/java/io/mybatis/demo/mybatis/model/Role.java
    1679  Defl:N      581  65% 05-30-2021 18:17 1ef86c27  cms/src/main/java/io/mybatis/demo/mybatis/model/User.java
    1341  Defl:N      504  62% 05-30-2021 18:17 92c5bec1  cms/src/main/java/io/mybatis/demo/mybatis/model/UserRole.java
       0  Defl:N        2   0% 05-30-2021 18:17 00000000  cms/src/main/java/io/mybatis/demo/mybatis/service/
     235  Defl:N      153  35% 05-30-2021 18:17 efb596bd  cms/src/main/java/io/mybatis/demo/mybatis/service/RoleService.java
     235  Defl:N      153  35% 05-30-2021 18:17 59b608c0  cms/src/main/java/io/mybatis/demo/mybatis/service/UserService.java
     267  Defl:N      177  34% 05-30-2021 18:17 5121a031  cms/src/main/java/io/mybatis/demo/mybatis/service/UserRoleService.java
       0  Defl:N        2   0% 05-30-2021 18:17 00000000  cms/src/main/java/io/mybatis/demo/mybatis/service/impl/
     442  Defl:N      218  51% 05-30-2021 18:17 26c22e04  cms/src/main/java/io/mybatis/demo/mybatis/service/impl/RoleServiceImpl.java
     442  Defl:N      219  51% 05-30-2021 18:17 fdabfe3b  cms/src/main/java/io/mybatis/demo/mybatis/service/impl/UserServiceImpl.java
     490  Defl:N      247  50% 05-30-2021 18:17 742f3203  cms/src/main/java/io/mybatis/demo/mybatis/service/impl/UserRoleServiceImpl.java
       0  Defl:N        2   0% 05-30-2021 18:17 00000000  cms/src/main/resources/
       0  Defl:N        2   0% 05-30-2021 18:17 00000000  cms/src/main/resources/mappers/
     435  Defl:N      252  42% 05-30-2021 18:17 afff9e3d  cms/src/main/resources/mappers/RoleMapper.xml
     574  Defl:N      277  52% 05-30-2021 18:17 920e9299  cms/src/main/resources/mappers/UserMapper.xml
     505  Defl:N      253  50% 05-30-2021 18:17 ef08156b  cms/src/main/resources/mappers/UserRoleMapper.xml
--------          -------  ---                            -------
    8195             3930  52%                            29 files
```

其他参数可以自己尝试。

## 总结

睿Rui是一个非常灵活易用的代码生成器，包含的功能细节非常的多，本篇文档旨在入门，还有很多内容没有展示出来，后续会针对睿Rui提供一些进阶使用文档，后续的计划如下：

- 【文档】tableRules 表名、列名特殊配置配置和字典设置
- 【文档】使用 睿Rui 生成完整项目  
- 【直播】分享 睿Rui-cli 代码生成器配置和使用
- 【文档】睿Rui-tpl 代码模板生成器的使用文档（生成代码生成器配置的工具）
- 【直播】以比较火的某个框架作为示例使用 睿Rui-tpl
- 【文档】睿Rui-gui 代码生成器GUI客户端使用说明
- 【工具】睿Rui-idea-plugin IDEA插件，结合IDEA的datasource使用代码生成器  
- 【工具】睿Rui-SaaS 代码生成器发布，提供模板市场
