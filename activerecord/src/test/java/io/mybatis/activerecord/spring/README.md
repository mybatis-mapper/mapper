# 测试说明

当使用 ActiveRecord 时，不建议存在多数据源。

为了避免真的需要多数据源，因此唯一的这个测试直接使用多数据源，这个实例只是为了测试，不适合直接在生产环境照搬使用。

当前测试用例为多数据源，因为在同一个包下面，扫描时为了区分开，增加两个 Marker 接口：

- UserMarker
- RoleMarker

在配置扫描时，指定了扫描那些接口：

```xml

<bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
  <property name="basePackage" value="io.mybatis.activerecord"/>
  <property name="markerInterface" value="io.mybatis.activerecord.UserMarker"/>
  <property name="sqlSessionFactoryBeanName" value="sqlSessionFactoryUser"/>
</bean>

<bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
<property name="basePackage" value="io.mybatis.activerecord"/>
<property name="markerInterface" value="io.mybatis.activerecord.RoleMarker"/>
<property name="sqlSessionFactoryBeanName" value="sqlSessionFactoryRole"/>
</bean>
```

在 `BaseId` 实现中，使用了 `database-schema-user.sql` 对应的数据源：

```xml

<bean id="springMapperRegistryUser" class="io.mybatis.activerecord.spring.MapperProvider">
  <constructor-arg index="0" ref="sqlSessionTemplateUser"/>
</bean>
```

```java
@Override
public Mapper<T, Integer> baseMapper(){
    //获取指定数据源的实例
    return SpringMapperRegistry.<T, Integer, Mapper<T, Integer>>getInstance("springMapperRegistryUser").baseMapper(entityClass());
    }
```