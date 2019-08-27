package nh.graphql.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@ServletComponentScan
public class TasksApplication {

  private static final Logger logger = LoggerFactory.getLogger(TasksApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(TasksApplication.class, args);
  }

  @Bean
  public CommandLineRunner importInitialData(Importer importer, RxStreamsTaskSubscriber taskSubscriber,
      GraphQLTaskSubscriber graphQLTaskSubscriber) {
    return args -> {
      logger.info("====== IMPORTING DATA ======= ");
      importer.add();
      logger.info("====== SERVER RUNNING ======= ");
    };

  }
//  /*
//   * Make sure Hibernate Sessions remains open for the whole request - We need
//   * this to lazy load referenced objects (e.g. project -> tasks, project ->
//   * categories) - We will implement a better solution soon
//   */
//  @Bean
//  public OpenEntityManagerInViewFilter openEntityManagerInViewFilter() {
//    return new OpenEntityManagerInViewFilter();
//  }

  @Bean
  WebMvcConfigurer webConfig() {
    return new WebMvcConfigurer() {
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowCredentials(true);
      }
    };
  }

  @Bean
  public FilterRegistrationBean<CorsFilter> corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOrigin("http://localhost:5080");
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    source.registerCorsConfiguration("/**", config);
    FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<CorsFilter>(new CorsFilter(source));
    bean.setOrder(0);
    return bean;
  }

  /**
   * RestTemplate needed for accessing the remote USerService
   * 
   * @param builder
   * @return
   */
  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }
}
