/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mybatis.activerecord.spring;

import io.mybatis.mapper.BaseMapper;
import io.mybatis.provider.EntityClassFinder;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 根据实体类提供对应的 Mapper 接口实例
 *
 * @param <T> 实体类类型
 * @param <I> 主键类型
 * @param <M> 实体对应的 Mapper
 * @author liuzh
 */
public class MapperProvider<T, I extends Serializable, M extends BaseMapper<T, I>> implements ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {
  /**
   * Spring 上下文
   */
  protected static ApplicationContext              applicationContext;
  /**
   * 缓存默认注入的实例
   */
  protected        Map<Class<?>, BaseMapper<T, I>> modelMapper = new ConcurrentHashMap<>();
  /**
   * 必须使用线程安全的 {@link SqlSessionTemplate}
   */
  protected        SqlSessionTemplate              sqlSessionTemplate;

  /**
   * 构造方法，必须使用线程安全的 {@link SqlSessionTemplate}
   *
   * @param sqlSessionTemplate 线程安全的 {@link SqlSessionTemplate}
   */
  public MapperProvider(SqlSessionTemplate sqlSessionTemplate) {
    this.sqlSessionTemplate = sqlSessionTemplate;
  }

  /**
   * 获取默认实例
   *
   * @return 默认实例
   */
  public static <T, I extends Serializable, M extends BaseMapper<T, I>> MapperProvider<T, I, M> getDefaultInstance() {
    return MapperProviderInstance.getINSTANCE();
  }

  /**
   * 获取指定的实例
   *
   * @return 默认实例
   */
  public static <T, I extends Serializable, M extends BaseMapper<T, I>> MapperProvider<T, I, M> getInstance(String instanceName) {
    return (MapperProvider<T, I, M>) applicationContext.getBean(instanceName);
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    MapperProvider.applicationContext = applicationContext;
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    this.initMapper();
  }

  protected void initMapper() {
    this.sqlSessionTemplate.getConfiguration().getMapperRegistry().getMappers().forEach(mapper -> {
      addMapper(mapper, this.sqlSessionTemplate.getMapper(mapper));
    });
  }

  /**
   * 添加 Mapper 接口和实例（可选）
   *
   * @param type   Mapper 接口类
   * @param mapper Mapper 实例
   */
  public void addMapper(Class<?> type, Object mapper) {
    if (type != null && mapper != null && BaseMapper.class.isAssignableFrom(type)) {
      EntityClassFinder.find(type, null).ifPresent(clazz -> {
        if (!modelMapper.containsKey(clazz)) {
          modelMapper.put(clazz, (BaseMapper<T, I>) mapper);
        }
      });
    }
  }

  /**
   * 获取实体类对应的 Mapper 接口
   *
   * @param modelClass 实体类
   * @return Mapper 接口
   */
  public M baseMapper(Class<T> modelClass) {
    if (!modelMapper.containsKey(modelClass)) {
      synchronized (this) {
        if (!modelMapper.containsKey(modelClass)) {
          this.initMapper();
        }
      }
    }
    if (modelMapper.containsKey(modelClass)) {
      return (M) modelMapper.get(modelClass);
    }
    throw new RuntimeException(modelClass.getName() + " Mapper interface not found");
  }

  /**
   * 将当前实例设置为默认实例
   */
  public void registerAsDefault() {
    MapperProviderInstance.setINSTANCE(this);
  }

  private static class MapperProviderInstance {
    public static MapperProvider INSTANCE;

    private static <T, I extends Serializable, M extends BaseMapper<T, I>> MapperProvider<T, I, M> getINSTANCE() {
      if (INSTANCE == null) {
        throw new NullPointerException("MapperProvider default instance not found");
      }
      return INSTANCE;
    }

    private static <T, I extends Serializable, M extends BaseMapper<T, I>> void setINSTANCE(MapperProvider<T, I, M> INSTANCE) {
      MapperProviderInstance.INSTANCE = INSTANCE;
    }
  }

}
