package com.lonelyash.common.config;

import com.lonelyash.common.advice.CommonExceptionAdvice;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import(CommonExceptionAdvice.class)
public class ExceptionConfig {
}
