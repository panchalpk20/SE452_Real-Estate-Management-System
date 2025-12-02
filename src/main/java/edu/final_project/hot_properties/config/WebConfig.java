package edu.final_project.hot_properties.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class that implements {@link WebMvcConfigurer} interface to set up custom resource
 * handling for static files like CSS and images.
 */

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // for property
//    @Value("${hot.properties.upload-dir}")
//    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
        // for property
         registry.addResourceHandler("/images/uploads/**")
                .addResourceLocations("file:uploads/");

    }
}