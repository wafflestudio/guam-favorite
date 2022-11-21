package waffle.guam.favorite.client

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.web.reactive.function.client.WebClient
import waffle.guam.favorite.client.impl.GuamFavoriteBlockingClientImpl
import waffle.guam.favorite.client.impl.GuamFavoriteClientImpl

@EnableConfigurationProperties(GuamFavoriteProperties::class)
@Configuration
class GuamFavoriteAutoConfiguration {

    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnMissingBean(GuamFavoriteClient.Blocking::class)
    @Bean
    fun guamFavoriteBlockingClient(
        properties: GuamFavoriteProperties,
        builder: WebClient.Builder,
        env: Environment,
    ): GuamFavoriteClient.Blocking {
        return GuamFavoriteBlockingClientImpl(
            url = properties.url ?: url(env),
            builder = builder,
            fallback = properties.fallback
        )
    }

    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @ConditionalOnMissingBean(GuamFavoriteClient::class)
    @Bean
    fun guamFavoriteClient(
        properties: GuamFavoriteProperties,
        builder: WebClient.Builder,
        env: Environment,
    ): GuamFavoriteClient {
        return GuamFavoriteClientImpl(
            url = properties.url ?: url(env),
            builder = builder,
            fallback = properties.fallback
        )
    }

    fun url(env: Environment) = when {
        env.acceptsProfiles(Profiles.of("dev")) -> "http://guam-favorite-api.wafflestudio.com"
        else -> TODO()
    }
}
