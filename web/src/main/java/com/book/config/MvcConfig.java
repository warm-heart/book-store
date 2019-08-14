package com.book.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
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

        //book
        registry.addViewController("/book/toAddBook").setViewName("book/addBook");
    }

}
