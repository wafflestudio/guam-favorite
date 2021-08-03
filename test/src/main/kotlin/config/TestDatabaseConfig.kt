package waffle.guam.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.datasource.init.DataSourceInitializer
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
import javax.sql.DataSource

@Configuration
class TestDatabaseConfig @Autowired constructor(
    @Qualifier("databaseSourceInfo") val databaseSourceInfo: DatabaseSourceInfo?,
    @Qualifier("extraDatabaseSourceInfo") val extraDatabaseSourceInfo: DatabaseSourceInfo?
) {
    @Bean
    fun dataSourceInitializer(dataSource: DataSource): DataSourceInitializer {
        val resourceInfo =
            (databaseSourceInfo?.sources ?: emptyList()).let { it.plus(extraDatabaseSourceInfo?.sources ?: emptyList()) }

        val resources = resourceInfo.map { ClassPathResource(it) }.toTypedArray()

        val dataSourceInitializer = DataSourceInitializer()
        dataSourceInitializer.setDataSource(dataSource)
        dataSourceInitializer.setDatabasePopulator(ResourceDatabasePopulator(*resources))

        return dataSourceInitializer
    }
}
