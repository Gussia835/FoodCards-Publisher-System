package org.example.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "org.example.repository.pom",
        entityManagerFactoryRef = "pomEntityManagerFactory",
        transactionManagerRef = "pomTransactionManager"
)
public class PomDataSourceConfig {
    @Value("${receiver.datasource.pom.url}")
    private String url;

    @Value("${receiver.datasource.pom.username}")
    private String user;

    @Value("${receiver.datasource.pom.password}")
    private String password;

    @Value("${receiver.datasource.pom.driver-class-name}")
    private String driverName;

    @Primary
    @Bean(name = "pomDataSource")
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .driverClassName(driverName)
                .url(url)
                .username(user)
                .password(password)
                .build();
    }

    @Primary
    @Bean(name = "pomEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
                                                EntityManagerFactoryBuilder builder,
                                                @Qualifier("pomDataSource") DataSource dataSource) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.hbm2ddl.create_namespaces", "true");

        return builder
                .dataSource(dataSource)
                .packages("org.example.models.pom")
                .persistenceUnit("pom")
                .properties(properties)
                .build();
    }

    @Primary
    @Bean(name = "pomTransactionManager")
    public PlatformTransactionManager transactionManager(
                                            @Qualifier("pomEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}