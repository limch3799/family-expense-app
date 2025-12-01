package com.example.d105.common.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {
                "com.example.d105.domain.user.repository",
                "com.example.d105.domain.account.repository",
                "com.example.d105.domain.transaction.repository",
                "com.example.d105.domain.group.repository",
                "com.example.d105.domain.tracking.repository",
                "com.example.d105.domain.report.repository"
        },
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
public class PrimaryJpaConfig {

        @Value("${db.primary.url}")
        private String url;

        @Value("${db.primary.username}")
        private String username;

        @Value("${db.primary.password}")
        private String password;

        @Value("${db.primary.max-pool-size}")
        private int maxPoolSize;

        @Value("${db.primary.min-idle}")
        private int minIdle;

        @Value("${db.primary.connection-timeout}")
        private long connectionTimeout;

        @Value("${db.primary.idle-timeout}")
        private long idleTimeout;

        @Bean(name = "dataSource")
        @Primary
        public DataSource dataSource() {
                HikariDataSource dataSource = new HikariDataSource();
                dataSource.setJdbcUrl(url);
                dataSource.setUsername(username);
                dataSource.setPassword(password);
                dataSource.setDriverClassName("org.postgresql.Driver");
                dataSource.setMaximumPoolSize(maxPoolSize);
                dataSource.setMinimumIdle(minIdle);
                dataSource.setConnectionTimeout(connectionTimeout);
                dataSource.setIdleTimeout(idleTimeout);
                return dataSource;
        }

        @Bean(name = "entityManagerFactory")
        @Primary
        public LocalContainerEntityManagerFactoryBean entityManagerFactory(
                EntityManagerFactoryBuilder builder,
                DataSource dataSource) {

                return builder
                        .dataSource(dataSource)
                        .packages("com.example.d105.domain")
                        .persistenceUnit("primary")
                        .properties(hibernateProperties())
                        .build();
        }

        @Bean(name = "transactionManager")
        @Primary
        public PlatformTransactionManager transactionManager(
                EntityManagerFactory entityManagerFactory) {
                return new JpaTransactionManager(entityManagerFactory);
        }

        private Map<String, Object> hibernateProperties() {
                Map<String, Object> properties = new HashMap<>();
                properties.put("hibernate.jdbc.time_zone", "Asia/Seoul");
                properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
                properties.put("hibernate.hbm2ddl.auto", "update");
                properties.put("hibernate.show_sql", "true");
                properties.put("hibernate.format_sql", "true");
                return properties;
        }
}