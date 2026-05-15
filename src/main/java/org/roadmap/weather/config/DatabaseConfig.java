package org.roadmap.weather.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.hibernate.HibernateTransactionManager;
import org.springframework.orm.jpa.hibernate.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {
    private final Environment env;

    public DatabaseConfig(Environment env) {
        this.env = env;
    }

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.postgresql.Driver");
        config.setJdbcUrl(env.getProperty("db.url"));
        config.setUsername(env.getProperty("db.username"));
        config.setPassword(env.getProperty("db.password"));

        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);

        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        return new HikariDataSource(config);
    }

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        String schema = env.getProperty("db.schema");
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("db/migration")
                .schemas(schema)
                .defaultSchema(schema)
                .baselineOnMigrate(true)
                .load();
    }

    @Bean
    @DependsOn("flyway")
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPackagesToScan("org.roadmap.weather.entity");
        Properties properties = new Properties();
        properties.setProperty("hibernate.type.preferred_uuid_jdbc_type", "CHAR");
        properties.setProperty("hibernate.hbm2ddl.auto", "validate");
        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("hibernate.format_sql", "true");

        String schema = env.getProperty("db.schema");
        properties.setProperty("hibernate.default_schema", schema);

        properties.setProperty("hibernate.jdbc.batch_size", "20");
        properties.setProperty("hibernate.jdbc.fetch_size", "100");

        sessionFactory.setHibernateProperties(properties);
        return sessionFactory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(SessionFactory sessionFactory) {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory);
        return transactionManager;
    }
}
