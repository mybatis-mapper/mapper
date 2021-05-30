# `睿Rui` - 代码生成器

本项目直接使用了一个功能强大的代码生成器 `睿Rui`，这个代码生成器可以设置项目的目录结构和具体的代码模板，
可以方便的从零生成一个完整的项目，也可以在已有项目结构中生成指定的代码文件。

## 项目文件简介

所有内容都在 `src/main/resources` 下面：
- `lib` 目录，代码生成器和数据库驱动 jar 包放到该目录下
  - `mysql-connector-java-5.1.49.jar` MySQL驱动，当前只提供了MySQL驱动，
    自己可以替换版本，连接其他数据库时，将对应的驱动放到当前 lib 目录下，
    你可以把自己常用的各种数据库驱动都放在这里。
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

这部分内容需要下载使用，为了方便使用，提供一个压缩包直接下载使用。

> **百度网盘**  
> 链接: https://pan.baidu.com/s/1fzxiZaDB9zT0mUW7NKiQTA   
> 提取码: kgtk  
> 
> **GitHub**  
> https://github.com/mybatis-mapper/mapper/blob/master/generator/rui-cli.zip  
> 
> **Gitee**(需登录)  
> https://gitee.com/mybatis-mapper/mapper/blob/master/generator/rui-cli.zip  
> 
> 数据库驱动部分提供一些链接，可以根据需要进行下载：
> - [MySQL Connector/J](https://mvnrepository.com/artifact/mysql/mysql-connector-java)
> - [MariaDB Java Client](https://mvnrepository.com/artifact/org.mariadb.jdbc/mariadb-java-client)
> - [PostgreSQL JDBC Driver](https://mvnrepository.com/artifact/org.postgresql/postgresql)
> - [Microsoft JDBC Driver For SQL Server](https://mvnrepository.com/artifact/com.microsoft.sqlserver/mssql-jdbc)
> - [JTDS for Microsoft SQL Server](https://mvnrepository.com/artifact/net.sourceforge.jtds/jtds)
> - [Oracle JDBC Driver - Ojdbc10](https://mvnrepository.com/artifact/com.oracle.database.jdbc/ojdbc10)
> - [SQLite JDBC](https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc)
> - [Apache Derby Client JDBC Driver](https://mvnrepository.com/artifact/org.apache.derby/derbyclient)
> - [ClickHouse JDBC](https://mvnrepository.com/artifact/ru.yandex.clickhouse/clickhouse-jdbc)
> - [DB2 - IBM Data Server Driver For JDBC and SQLJ](https://mvnrepository.com/artifact/com.ibm.db2/jcc)
> - [Informix Driver](https://mvnrepository.com/artifact/com.informix/informix-driver)

## 代码生成器配置 project.yaml

下面通过注释介绍每一项配置的含义或者作用：
```yaml
# 模板的名字，生成代码的根目录名称
name: mybatis-mapper-demo
# 代码生成的路径，可以使用 SYS 和 ENV 等变量，具体包含的内容和运行环境有关
# user.dir 为当前运行命令的目录，path 不设置时的默认值也是当前路径
# 当 path 路径以 .zip 后缀结尾时，会将生成的目录结构和代码放到压缩包中
#【命令行】可以通过 —o 重新指定位置
path: ${SYS['user.dir']}/
# 模板文件所在路径，默认为相对 path 所在的位置，不设置时和 path 相同
#【命令行】可以通过 —T 重新指定位置
templates: mapper-templates
# 数据库配置
database: 
  # 数据库连接配置
  jdbcConnection:
    # 使用方言，默认为 JDBC 方式，可能会取不到表或字段注释
    # 后续介绍如何通过字典匹配注释值
    #【命令行】可以通过 --jdbc.dialect 覆盖
    dialect: MYSQL
    # jdbc驱动
    #【命令行】可以通过 --jdbc.driver 覆盖
    driver: com.mysql.jdbc.Driver
    # jdbc连接地址
    #【命令行】可以通过 --jdbc.url 覆盖
    url: jdbc:mysql://localhost:3306/test?useSSL=false
    # 用户名
    #【命令行】可以通过 --jdbc.user 覆盖
    user: root
    # 密码
    #【命令行】可以通过 --jdbc.password 覆盖
    password: root
  # 获取表配置，支持 % 和 _ 模糊匹配，可以配置多个值  
  # 还有一个支持复杂规则 tableRules 属性后续单独介绍
  #【命令行】可以通过 -t table1,table2 覆盖
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
#【命令行】可以通过 -AbasePackage=com.company 覆盖或增加新属性
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
- `times: 1` 可选配置 times，默认值 1，默认情况下，模板只经过一次变量替换，有时存在多层变量嵌套时，执行多次才能全部替换

>上面示例中的 `iterFilter: it.name.original.o.startsWith("sys")` 中的 `it.name.original.o` 很奇怪，下面模板介绍为什么会这么写。

## 模板文件介绍

目前代码模板使用 `FreeMarker` 模板引擎，点击查看 [模板语言参考文档](http://www.kerneler.com/freemarker2.3.23/ref.html)。

针对 model 提供了 4 套模板：

- `extend` 基于 `@Extend` 注解的代码模板
  - `model.java`  普通的实体类模板
  - `model-lombok.java` 使用 lombok 注解的实体类模板
- `simple` 基于 `@Entity` 注解的代码模板
  - `model.java`  普通的实体类模板
  - `model-lombok.java` 使用 lombok 注解的实体类模板

修改配置文件下面的地方选择想要使用的文档：

```yaml
# 在 model 包下面
- name: model
  files:
    # 有实体类代码，类名为表名的类形式
    - name: '${it.name.className}.java'
      # 此处有4种模板示例，具体类型看上面介绍
      # file: simple/model.java
      # file: simple/model-lombok.java
      file: extend/model.java
      # file: extend/model-lombok.java
      iter: tables
```

当使用 lombok 版本的模板时，实体类使用了 `@Getter` 和 `@Setter` 注解，代码体中就不需要生成 getter 和 setter 的代码。
如果你想使用 lombok 的 `@Data` 或其他注解，你可以实现自己的模板（注意 `import` 相应的类型）。

## ENV 和 SYS 可选值，使用示例

代码生成器配置和模板中都可以使用 ENV 和 SYS 变量，这些变量和运行的环境有关，
代码生成器运行时会输出当前环境的这些信息，想要修改为某些值时可以运行之后查找想要使用的名字。
例如下面展示的**部分内容**（示例为 macOS）：
```
[main] TRACE Project - SYS可用参数:
[main] TRACE Project - SYS['user.country'] = CN
[main] TRACE Project - SYS['user.home'] = /Users/liuzh
[main] TRACE Project - SYS['user.dir'] = /Users/liuzh/IdeaProjects/rui/core

[main] TRACE Project - ENV可用参数:
[main] TRACE Project - ENV['USER'] = liuzh
[main] TRACE Project - ENV['PWD'] = /Users/liuzh/IdeaProjects/rui/core
[main] TRACE Project - ENV['HOME'] = /Users/liuzh
```
在模板中使用时，直接参考上面的写法，例如 `${SYS['user.home']}` 和 `${ENV['HOME']}`。

## 代码生成器 rui-cli.jar

代码生成器提供了一个可执行 jar 文件: rui-cli.jar

> 后续会提供基于 java swing 的 GUI 客户端，可以通过 UI 简单操作生成代码。

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

上面介绍了部分代码生成器的细节，最关键的内容是项目结构和代码模板，
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
根据自己的包名修改这里，例如：`com.company.cms`，如果你的包和这里结构不一样，可以自己调整 `files` 适配自己的结构。

#### 类名规则

在 mapper 包下面循环生成了每个表对应的 Mapper 接口，这里定义的名称为 `'${it.name.className}Mapper.java'`，
`表的类名`+`Mapper` 后缀作为接口名，如果你喜欢使用 `Dao` 后缀，就可以改为 `'${it.name.className}Dao.java'`，
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

- 【文档】编写自己的代码模板
- 【文档】项目结构和代码的配置
- 【文档】tableRules 表名、列名特殊配置配置和字典设置
- 【文档】MERGE 合并文件用法介绍
- 【文档】使用 睿Rui 生成完整项目  
- 【直播】分享 睿Rui-cli 代码生成器配置和使用
- 【文档】睿Rui-tpl 代码模板生成器的使用文档（生成代码生成器配置的工具）
- 【直播】以比较火的某个框架作为示例使用 睿Rui-tpl
- 【文档】睿Rui-gui 代码生成器GUI客户端使用说明
- 【工具】睿Rui-idea-plugin IDEA插件，结合IDEA的datasource使用代码生成器  
- 【工具】睿Rui-SaaS 代码生成器发布，提供模板市场
