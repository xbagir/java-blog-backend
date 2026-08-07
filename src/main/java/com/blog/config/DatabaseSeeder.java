package com.blog.config;

import com.blog.repository.PostRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DatabaseSeeder {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final DataSource dataSource;
    private final PostRepository postRepository;
    private final boolean seedDatabase;

    public DatabaseSeeder(DataSource dataSource,
                          PostRepository postRepository,
                          @Value("${app.seed-database:true}") boolean seedDatabase) {
        this.dataSource = dataSource;
        this.postRepository = postRepository;
        this.seedDatabase = seedDatabase;
    }

    @PostConstruct
    public void initialize() {
        DatabasePopulatorUtils.execute(new ResourceDatabasePopulator(new ClassPathResource("schema.sql")), dataSource);

        if (seedDatabase && postRepository.count() == 0) {
            log.info("Seeding demo data into the database...");
            DatabasePopulatorUtils.execute(new ResourceDatabasePopulator(new ClassPathResource("data.sql")), dataSource);
        }
    }
}
