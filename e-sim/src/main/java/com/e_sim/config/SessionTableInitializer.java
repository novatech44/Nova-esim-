package com.e_sim.config;

//import org.springframework.boot.CommandLineRunner;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Component;
//
//@Component
//public class SessionTableInitializer implements CommandLineRunner {
//
//    private final JdbcTemplate jdbcTemplate;
//
//    public SessionTableInitializer(JdbcTemplate jdbcTemplate) {
//        this.jdbcTemplate = jdbcTemplate;
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        // Create session tables if they don't exist
//        createSessionTables();
//    }
//
//    private void createSessionTables() {
//        try {
//            // Create SPRING_SESSION table
//            jdbcTemplate.execute(
//                    "CREATE TABLE IF NOT EXISTS SPRING_SESSION (" +
//                            "  PRIMARY_ID CHAR(36) NOT NULL," +
//                            "  SESSION_ID CHAR(36) NOT NULL," +
//                            "  CREATION_TIME BIGINT NOT NULL," +
//                            "  LAST_ACCESS_TIME BIGINT NOT NULL," +
//                            "  MAX_INACTIVE_INTERVAL INT NOT NULL," +
//                            "  EXPIRY_TIME BIGINT NOT NULL," +
//                            "  PRINCIPAL_NAME VARCHAR(100)," +
//                            "  CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)" +
//                            ")"
//            );
//
//            // Create index on SESSION_ID
//            jdbcTemplate.execute(
//                    "CREATE UNIQUE INDEX IF NOT EXISTS SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID)"
//            );
//
//            // Create index on EXPIRY_TIME for cleanup
//            jdbcTemplate.execute(
//                    "CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME)"
//            );
//
//            // Create index on PRINCIPAL_NAME
//            jdbcTemplate.execute(
//                    "CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME)"
//            );
//
//            // Create SPRING_SESSION_ATTRIBUTES table
//            jdbcTemplate.execute(
//                    "CREATE TABLE IF NOT EXISTS SPRING_SESSION_ATTRIBUTES (" +
//                            "  SESSION_PRIMARY_ID CHAR(36) NOT NULL," +
//                            "  ATTRIBUTE_NAME VARCHAR(200) NOT NULL," +
//                            "  ATTRIBUTE_BYTES BLOB NOT NULL," +
//                            "  CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME)," +
//                            "  CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE" +
//                            ")"
//            );
//
//            System.out.println("Session tables created successfully");
//        } catch (Exception e) {
//            System.err.println("Error creating session tables: " + e.getMessage());
//        }
//    }
//}
