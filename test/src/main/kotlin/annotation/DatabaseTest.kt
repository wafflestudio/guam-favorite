package waffle.guam.annotation

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import waffle.guam.config.DatabaseSourceRegistrar
import waffle.guam.config.TestDatabaseConfig

@ActiveProfiles("test")
@Import(value = [TestDatabaseConfig::class, DatabaseSourceRegistrar::class])
@DataJpaTest
annotation class DatabaseTest(val databaseResources: Array<String> = [])
