package com.blog;

import com.blog.config.AppConfig;
import com.blog.config.WebConfig;
import jakarta.servlet.MultipartConfigElement;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.nio.file.Files;

public final class BlogApplication {

    private static final Logger log = LoggerFactory.getLogger(BlogApplication.class);

    private static final long MAX_UPLOAD_SIZE = 10L * 1024 * 1024;

    private BlogApplication() {
    }

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(Files.createTempDirectory("blog-tomcat").toString());
        tomcat.setPort(port);
        tomcat.getConnector();

        Context tomcatContext = tomcat.addContext("", null);
        tomcatContext.setParentClassLoader(BlogApplication.class.getClassLoader());

        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(tomcatContext.getServletContext());
        context.register(AppConfig.class, WebConfig.class);
        context.refresh();
        Runtime.getRuntime().addShutdownHook(new Thread(context::close));

        DispatcherServlet dispatcherServlet = new DispatcherServlet(context);
        dispatcherServlet.setThrowExceptionIfNoHandlerFound(true);

        Wrapper dispatcher = Tomcat.addServlet(tomcatContext, "dispatcher", dispatcherServlet);
        dispatcher.setMultipartConfigElement(new MultipartConfigElement(
                System.getProperty("java.io.tmpdir"), MAX_UPLOAD_SIZE, MAX_UPLOAD_SIZE, 0));
        tomcatContext.addServletMappingDecoded("/", "dispatcher");

        tomcat.start();
        log.info("Blog backend started on http://localhost:{}/", port);
        tomcat.getServer().await();
    }
}
