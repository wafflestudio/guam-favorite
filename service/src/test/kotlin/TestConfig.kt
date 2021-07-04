package waffle.guam

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
class TestApplication

@EntityScan(basePackages = ["waffle.guam"])
@EnableJpaRepositories(basePackages = ["waffle.guam"])
@ComponentScan(basePackages = ["waffle.guam"])
@SpringBootTest
annotation class DatabaseTest
