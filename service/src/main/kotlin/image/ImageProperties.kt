package waffle.guam.image

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("image")
data class ImageProperties(
    val root: String = "TEST"
)
