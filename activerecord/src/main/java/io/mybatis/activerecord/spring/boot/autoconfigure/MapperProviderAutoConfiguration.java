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

package io.mybatis.activerecord.spring.boot.autoconfigure;

import io.mybatis.activerecord.spring.MapperProvider;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自动配置 {@link MapperProvider}
 *
 * @author liuzh
 */
@Configuration
@ConditionalOnProperty(prefix = MapperProviderProperties.PREFIX, name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(MapperProviderProperties.class)
public class MapperProviderAutoConfiguration {

  /**
   * 当前没有自定义的 Mapper 提供者时，提供一个默认的
   * <p>
   * 当使用多数据源时，需要通过 {@link org.springframework.context.annotation.Primary} 注解指定主要的 {@link SqlSessionTemplate}
   *
   * @param sqlSessionTemplate
   * @return
   */
  @Bean
  @ConditionalOnMissingBean
  public MapperProvider springMapperRegistry(SqlSessionTemplate sqlSessionTemplate) {
    return new MapperProvider(sqlSessionTemplate);
  }

  /**
   * 自动注册为默认的 Mapper 提供者
   * <p>
   * 当使用多数据源时，需要通过 {@link org.springframework.context.annotation.Primary} 注解指定主要的默认的 {@link MapperProvider}
   */
  @Configuration
  public static class AutoRegisterConfiguration implements InitializingBean {
    private final MapperProvider mapperProvider;

    public AutoRegisterConfiguration(MapperProvider mapperProvider) {
      this.mapperProvider = mapperProvider;
    }

    @Override
    public void afterPropertiesSet() {
      mapperProvider.registerAsDefault();
    }
  }

}
