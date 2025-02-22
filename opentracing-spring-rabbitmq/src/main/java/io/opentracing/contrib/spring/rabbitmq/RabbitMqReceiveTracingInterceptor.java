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

import io.opentracing.Scope;
import io.opentracing.Tracer;

import java.util.Optional;
import lombok.RequiredArgsConstructor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.aop.AfterAdvice;
import org.springframework.aop.BeforeAdvice;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author Gilles Robert
 */
@RequiredArgsConstructor
class RabbitMqReceiveTracingInterceptor implements MethodInterceptor, AfterAdvice, BeforeAdvice {

  private final Tracer tracer;
  private final RabbitMqSpanDecorator spanDecorator;
  
  @Value("${spring.rabbitmq.messagebody.in.spans:false}")
  private Optional<Boolean> addMessagesToSpans = Optional.of(false);

  @Override
  public Object invoke(MethodInvocation methodInvocation) throws Throwable {
    Message message = (Message) methodInvocation.getArguments()[1];
    MessageProperties messageProperties = message.getMessageProperties();

    Optional<Scope> child = RabbitMqTracingUtils.buildReceiveSpan(messageProperties, tracer);
    if (addMessagesToSpans.isPresent() && addMessagesToSpans.get()) {
      child.ifPresent(scope -> spanDecorator.onReceive(messageProperties, scope.span(), message));
    } else {
      child.ifPresent(scope -> spanDecorator.onReceive(messageProperties, scope.span()));
    }

    // CHECKSTYLE:OFF
    try {
      return methodInvocation.proceed();
    } catch (Exception ex) {
      // CHECKSTYLE:ON
      child.ifPresent(scope -> spanDecorator.onError(ex, scope.span()));
      throw ex;
    } finally {
      child.ifPresent(Scope::close);
    }
  }
}
