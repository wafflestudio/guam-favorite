package waffle.guam.annotation

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.test.context.ActiveProfiles
import waffle.guam.config.DatabaseSourceRegistrar
import waffle.guam.config.TestDatabaseConfig

@ActiveProfiles("test")
@EntityScan(basePackages = ["waffle.guam"])
@EnableJpaRepositories(basePackages = ["waffle.guam"])
@ComponentScan(basePackages = ["waffle.guam"])
@Import(value = [TestDatabaseConfig::class, DatabaseSourceRegistrar::class])
@SpringBootTest
annotation class DatabaseTest(val databaseResources: Array<String> = [])
