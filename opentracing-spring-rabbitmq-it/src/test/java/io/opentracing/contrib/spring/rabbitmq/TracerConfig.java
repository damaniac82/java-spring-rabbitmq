/**
 * Copyright 2017-2019 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.opentracing.contrib.spring.rabbitmq;

import io.opentracing.mock.MockTracer;
import io.opentracing.util.GlobalTracerTestUtil;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * @author Ats Uiboupin
 */
@Configuration
public class TracerConfig {

  @Bean
  public MockTracer mockTracer() {
    GlobalTracerTestUtil.resetGlobalTracer();
    return new MockTracer();
  }
  
  @ConditionalOnMissingBean(PropertyPlaceholderConfigurer.class)
  @Bean
  public PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() {
    PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
    configurer.setLocations(new Resource[]{new ClassPathResource("/application.properties")});
    configurer.setIgnoreResourceNotFound(true);
    return configurer;
  }
  
}
