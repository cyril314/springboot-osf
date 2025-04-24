package com.fit.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * @className: WebMvcConfig
 * @description: 配置信息
 * @author: Aim
 * @date: 2023/8/1
 **/
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {

    private final String NO_INTERCEPTOR_PATH = ".*/((login)|(logout)|(code)|(app)|(weixin)|(assets)|(main)|(websocket)).*";

    @Value("${spring.mvc.view.prefix}")
    private String prefix = "/jsp/";
    @Value("${spring.mvc.view.suffix}")
    private String suffix = ".jsp";

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseSuffixPatternMatch(true);
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver(prefix, suffix);
        resolver.setExposeContextBeansAsAttributes(true);
        resolver.setCache(true);
        registry.viewResolver(resolver);
        resolver.setContentType("text/html;charset=UTF-8");
        registry.enableContentNegotiation(new MappingJackson2JsonView());
    }

    /**
     * 访问根路径默认跳转页面
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
    }

    /**
     * 添加静态资源
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/**").addResourceLocations("classpath:/META-INF/resources/assets/");
        registry.addResourceHandler("/html/**").addResourceLocations("classpath:/META-INF/resources/html/");
        registry.addResourceHandler("/uploadFiles/**").addResourceLocations("classpath:/uploadFiles/");
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/META-INF/resources/static/");
        registry.addResourceHandler("/templates/**").addResourceLocations("classpath:/templates/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("favicon.ico").addResourceLocations("classpath:/favicon.ico");
        super.addResourceHandlers(registry);
    }

    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        super.configureHandlerExceptionResolvers(exceptionResolvers);
        // 错误页面的处理
        exceptionResolvers.add((request, response, handler, ex) -> {
            ModelAndView mav = new ModelAndView();
            switch (response.getStatus()) {
                case 200:
                    String path = request.getServletPath();
                    if (!path.matches(NO_INTERCEPTOR_PATH)) {
                        String username = (String) request.getSession(true).getAttribute("username");
                        if (username == null || username.isEmpty()) {
                            mav.addObject("message", "未登录或登录超时!");
                            mav.setViewName("redirect:/login");
                        }
                    }
                    break;
                case 500:
                    mav.setViewName("error/500");
                    break;
                case 404:
                    mav.setViewName("error/404");
                    break;
                case 403:
                    mav.setViewName("error/403");
                    break;
            }
            mav.addObject("base", request.getContextPath());
            return mav;
        });
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang"); // 请求参数中的 lang 用于切换语言
        registry.addInterceptor(interceptor);
        registry.addInterceptor(loginInterceptor).order(2);
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) {
                req.setAttribute("ctx", req.getContextPath());
                if (req.getSession().getAttribute("user") != null) {
                    req.setAttribute("user", req.getSession().getAttribute("user"));
                }
                return true;
            }
        }).order(1).addPathPatterns("/**"); // 设置拦截的路径
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        //定义一个convert转换消息的对象
        FastJsonConfig config = new FastJsonConfig();
        config.setSerializerFeatures(
                // 保留map空的字段
                SerializerFeature.WriteMapNullValue,
                // 将String类型的null转成""
                SerializerFeature.WriteNullStringAsEmpty,
                // 将Number类型的null转成0
                SerializerFeature.WriteNullNumberAsZero,
                // 将List类型的null转成[]
                SerializerFeature.WriteNullListAsEmpty,
                // 将Boolean类型的null转成false
                SerializerFeature.WriteNullBooleanAsFalse,
                // 避免循环引用
                SerializerFeature.DisableCircularReferenceDetect);
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        //在convert中添加配置信息
        fastConverter.setFastJsonConfig(config);
        //设置支持的媒体类型
        fastConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON));
        //设置默认字符集
        fastConverter.setDefaultCharset(StandardCharsets.UTF_8);
        //将convert添加到converters
        converters.add(0, fastConverter);
        //解决返回字符串带双引号问题
        StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter();
        stringHttpMessageConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.TEXT_PLAIN));
        stringHttpMessageConverter.setDefaultCharset(StandardCharsets.UTF_8);
        converters.add(0, stringHttpMessageConverter);

        super.addDefaultHttpMessageConverters(converters);
    }

    /**
     * 跨域支持
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //对哪些目录可以跨域访问
        registry.addMapping("/**")
                //允许哪些网站可以跨域访问
                .allowedOrigins("*")
                //允许哪些方法
                .allowedMethods("GET", "POST", "DELETE", "PUT", "PATCH", "OPTIONS", "HEAD").maxAge(3600 * 24);
    }
}