package com.e_sim.config;

//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.jdbc.datasource.DataSourceTransactionManager;
//import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
//import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;
//import org.springframework.transaction.PlatformTransactionManager;
//
//import javax.sql.DataSource;
//
//@Configuration
//@EnableJdbcHttpSession(
//        tableName = "SPRING_SESSION",
//        maxInactiveIntervalInSeconds = 3600, // 1 hour
//        cleanupCron = "0 0 * * * *" // Clean up expired sessions every hour
//)
//public class JdbcSessionConfig extends AbstractHttpSessionApplicationInitializer {
//
//    @Bean
//    public PlatformTransactionManager transactionManager(DataSource dataSource) {
//        return new DataSourceTransactionManager(dataSource);
//    }
//}