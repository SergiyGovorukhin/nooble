package com.ghost;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Locale;

@SpringBootApplication
@PropertySource("classpath:application.properties")
public class NoobleApplication extends SpringBootServletInitializer {

	public static Logger log = LoggerFactory.getLogger(NoobleApplication.class.getName());

	public static void main(String[] args) {
		SpringApplication.run(NoobleApplication.class, args);
		log.info("Nooble started: {}", NoobleApplication.class);
		log.info("OS: {}", System.getProperty("os.name"));
		log.info("temp: {}", System.getProperty("java.io.tmpdir"));
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(NoobleApplication.class);
	}

	@Bean
	private static Configuration freemarkerConfiguration() {
		System.out.println(Configuration.class);
		Configuration configuration = new Configuration(freemarker.template.Configuration.VERSION_2_3_23);
		configuration.setClassForTemplateLoading(NoobleApplication.class, "templates");
		configuration.setDefaultEncoding("UTF-8");
		configuration.setLocale(Locale.US);
		configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		return configuration;
	}

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		super.onStartup(servletContext);
	}

	/**
	 * Resolves ${}
	 * @return PropertySourcesPlaceholderConfigurer bean
     */
	@Bean
	public PropertySourcesPlaceholderConfigurer configurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
}
