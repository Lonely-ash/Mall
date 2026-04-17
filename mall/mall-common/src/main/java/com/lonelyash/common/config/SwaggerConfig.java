package com.lonelyash.common.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
@ConditionalOnClass(Docket.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(SwaggerConfig.SwaggerProperties.class)
@ConditionalOnProperty(prefix = "mall.swagger", name = "package")
public class SwaggerConfig {

    @Bean
    @ConditionalOnMissingBean(Docket.class)
    public Docket defaultApi(SwaggerProperties properties) {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("mall-swagger")
                .apiInfo(apiInfo(properties))
                .select()
                .apis(RequestHandlerSelectors.basePackage(properties.getPackagePath()))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo(SwaggerProperties properties) {
        return new ApiInfoBuilder()
                .title(properties.getTitle())
                .description(properties.getDescription())
                .contact(new Contact(properties.getConcat(), properties.getUrl(), properties.getEmail()))
                .version(properties.getVersion())
                .build();
    }

    @Data
    @org.springframework.boot.context.properties.ConfigurationProperties(prefix = "mall.swagger")
    public static class SwaggerProperties {
        private String title;
        private String description;
        private String email;
        private String concat;
        private String url;
        private String version = "v1.0.0";
        private String packagePath;

        public void setPackage(String packagePath) {
            this.packagePath = packagePath;
        }
    }
}
