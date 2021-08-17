package waffle.guam

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GuamApplication

fun main(args: Array<String>) {
    runApplication<GuamApplication>(*args)
}
