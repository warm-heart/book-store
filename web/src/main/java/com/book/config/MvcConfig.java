package com.book.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author wangqianlong
 * @create 2019-04-11 12:28
 */
@Configuration

public class MvcConfig implements WebMvcConfigurer {


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        //可以直接访问静态资源 比如
        // http:localhost:8080/Image/bg-1565082763608.bg.jpg
        registry.addResourceHandler("/Image/**")
                .addResourceLocations("file:E:/bookImage/");

    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("/index");
        registry.addViewController("/user/toRegister").setViewName("user/register");
        registry.addViewController("/user/toLogin").setViewName("user/login");
        registry.addViewController("/user/toLogin1").setViewName("phoneLogin");


        //book
        registry.addViewController("/book/toAddBook").setViewName("book/addBook");
    }


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8082")
                .allowCredentials(true)
                .allowedMethods("POST", "GET", "PUT", "OPTIONS", "DELETE")
                .allowedHeaders("*")
                .maxAge(7200)
              ;

    }
}
