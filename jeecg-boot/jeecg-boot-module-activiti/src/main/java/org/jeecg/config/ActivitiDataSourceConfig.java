package org.jeecg.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.AbstractProcessEngineAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.Resource;
import javax.sql.DataSource;

@Configuration
public class ActivitiDataSourceConfig extends AbstractProcessEngineAutoConfiguration {
    @Resource
    private ActivitiDataSourceProperties activitiDataSourceProperties;

    @Bean
    public DataSource activitiDataSource() {
        DruidDataSource DruiddataSource = new DruidDataSource();
        DruiddataSource.setUrl(activitiDataSourceProperties.getUrl());
        DruiddataSource.setDriverClassName(activitiDataSourceProperties.getDriverClassName());
        DruiddataSource.setPassword(activitiDataSourceProperties.getPassword());
        DruiddataSource.setUsername(activitiDataSourceProperties.getUsername());
        return DruiddataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(activitiDataSource());
    }

    @Bean
    public SpringProcessEngineConfiguration springProcessEngineConfiguration() {
        SpringProcessEngineConfiguration configuration = new SpringProcessEngineConfiguration();
        configuration.setDataSource(activitiDataSource());
        configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        configuration.setJobExecutorActivate(true);
        configuration.setTransactionManager(transactionManager());
        configuration.setActivityFontName("宋体");
        configuration.setLabelFontName("宋体");
        configuration.setAnnotationFontName("宋体");
        //id生成器
        //configuration.setIdGenerator(new MyUUIDgenerator());
        return configuration;
    }


}
