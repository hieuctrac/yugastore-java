package com.yugabyte.app.yugastore.admin.config;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.SessionBuilderConfigurer;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.net.InetSocketAddress;
import java.util.Properties;

/**
 * Configuration for YugabyteDB dual database access:
 * - YSQL (PostgreSQL-compatible) for admin users and audit logs
 * - YCQL (Cassandra-compatible) for product catalog access
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.yugabyte.app.yugastore.admin.repository",
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager"
)
@EnableCassandraRepositories(
    basePackages = "com.yugabyte.app.yugastore.admin.repository"
)
public class YugabyteConfig extends AbstractCassandraConfiguration {

    @Value("${spring.datasource.url}")
    private String ysqlUrl;

    @Value("${spring.datasource.username}")
    private String ysqlUsername;

    @Value("${spring.datasource.password}")
    private String ysqlPassword;

    @Value("${spring.data.cassandra.keyspace-name}")
    private String ycqlKeyspace;

    @Value("${spring.data.cassandra.contact-points}")
    private String ycqlContactPoints;

    @Value("${spring.data.cassandra.port}")
    private int ycqlPort;

    @Value("${spring.data.cassandra.local-datacenter}")
    private String ycqlDatacenter;

    /**
     * YSQL DataSource configuration for admin users and audit logs
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(ysqlUrl);
        dataSource.setUsername(ysqlUsername);
        dataSource.setPassword(ysqlPassword);
        return dataSource;
    }

    /**
     * JPA EntityManagerFactory for YSQL entities
     */
    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("com.yugabyte.app.yugastore.admin.domain");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.setProperty("hibernate.hbm2ddl.auto", "validate");
        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("hibernate.format_sql", "true");
        em.setJpaProperties(properties);

        return em;
    }

    /**
     * Transaction manager for YSQL
     */
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }

    /**
     * YCQL Keyspace configuration
     */
    @Override
    protected String getKeyspaceName() {
        return ycqlKeyspace;
    }

    /**
     * YCQL Contact points configuration
     */
    @Override
    protected String getContactPoints() {
        return ycqlContactPoints;
    }

    /**
     * YCQL Port configuration
     */
    @Override
    protected int getPort() {
        return ycqlPort;
    }

    /**
     * YCQL Local datacenter configuration
     */
    @Override
    protected String getLocalDataCenter() {
        return ycqlDatacenter;
    }

    /**
     * YCQL Session builder configurer
     */
    @Override
    protected SessionBuilderConfigurer getSessionBuilderConfigurer() {
        return sessionBuilder -> sessionBuilder
            .addContactPoint(new InetSocketAddress(ycqlContactPoints, ycqlPort))
            .withLocalDatacenter(ycqlDatacenter);
    }

    /**
     * YCQL CassandraTemplate for direct YCQL operations
     */
    @Bean
    public CassandraTemplate cassandraTemplate(CqlSession cqlSession) {
        return new CassandraTemplate(cqlSession);
    }
}
