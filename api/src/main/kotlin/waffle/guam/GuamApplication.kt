package waffle.guam

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.io.ClassPathResource
import java.util.TimeZone
import javax.annotation.PostConstruct

@SpringBootApplication
class GuamApplication(
    private val dataInitializer: DataInitializer
) {
    @PostConstruct
    private fun init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
    }
}

fun main(args: Array<String>) {
    runApplication<GuamApplication>(*args)

    if (FirebaseApp.getApps().isEmpty()) {
        FirebaseApp.initializeApp(
            FirebaseOptions.builder()
                .setCredentials(
                    GoogleCredentials.fromStream(ClassPathResource("waffle-guam-firebase-adminsdk-1o1hg-27c33a640a.json").inputStream)
                )
                .build()
        )
    }
}
