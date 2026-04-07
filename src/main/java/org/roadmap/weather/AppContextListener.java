package org.roadmap.weather;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(DatabaseConfig.class);
        context.setServletContext(sce.getServletContext());
        context.scan("org.roadmap.weather");
        context.refresh();

        DispatcherServlet dispatcherServlet = new DispatcherServlet(context);
        sce.getServletContext().
                addServlet("dispatcher", dispatcherServlet)
                .addMapping("/");
    }
}
