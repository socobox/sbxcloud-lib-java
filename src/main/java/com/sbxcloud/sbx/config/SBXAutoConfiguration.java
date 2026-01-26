package com.sbxcloud.sbx.config;

import com.sbxcloud.sbx.client.SBXService;
import com.sbxcloud.sbx.client.SBXServiceFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for SBX Cloud client.
 * <p>
 * Automatically creates an {@link SBXService} bean when:
 * <ul>
 *   <li>The SBXService class is on the classpath</li>
 *   <li>No other SBXService bean is defined</li>
 *   <li>The {@code sbx.app-key} property is set</li>
 * </ul>
 *
 * <h3>Configuration</h3>
 * <pre>
 * # application.properties
 * sbx.app-key=your-app-key
 * sbx.token=your-bearer-token
 * sbx.domain=0
 * sbx.base-url=https://sbxcloud.com
 * sbx.debug=false
 * </pre>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * @Service
 * public class MyService {
 *     private final SBXService sbx;
 *
 *     public MyService(SBXService sbx) {
 *         this.sbx = sbx;
 *     }
 *
 *     public void doSomething() {
 *         var response = sbx.find(FindQuery.from("contact").compile(), Map.class);
 *         // ...
 *     }
 * }
 * }</pre>
 */
@AutoConfiguration
@ConditionalOnClass(SBXService.class)
@EnableConfigurationProperties(SBXProperties.class)
@ConditionalOnProperty(prefix = "sbx", name = "app-key")
public class SBXAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SBXService sbxService(SBXProperties properties) {
        return SBXServiceFactory.builder()
                .appKey(properties.getAppKey())
                .token(properties.getToken())
                .domain(properties.getDomain())
                .baseUrl(properties.getBaseUrl())
                .debug(properties.isDebug())
                .build();
    }
}
