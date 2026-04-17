package com.lonelyash.gateway.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SwaggerResourceController {

    @GetMapping("/swagger-resources")
    public List<Map<String, Object>> swaggerResources() {
        List<Map<String, Object>> resources = new ArrayList<>();
        resources.add(resource("user-service", "/user/v2/api-docs?group=mall-swagger", "/user/v2/api-docs"));
        resources.add(resource("item-service", "/item/v2/api-docs?group=mall-swagger", "/item/v2/api-docs"));
        resources.add(resource("cart-service", "/cart/v2/api-docs?group=mall-swagger", "/cart/v2/api-docs"));
        resources.add(resource("trade-service", "/trade/v2/api-docs?group=mall-swagger", "/trade/v2/api-docs"));
        resources.add(resource("pay-service", "/pay/v2/api-docs?group=mall-swagger", "/pay/v2/api-docs"));
        return resources;
    }

    @GetMapping("/swagger-resources/configuration/ui")
    public Map<String, Object> uiConfiguration() {
        return new HashMap<>(0);
    }

    @GetMapping("/swagger-resources/configuration/security")
    public Map<String, Object> securityConfiguration() {
        return new HashMap<>(0);
    }

    private Map<String, Object> resource(String name, String url, String location) {
        Map<String, Object> resource = new LinkedHashMap<>(4);
        resource.put("name", name);
        resource.put("url", url);
        resource.put("location", location);
        resource.put("swaggerVersion", "2.0");
        return resource;
    }
}
