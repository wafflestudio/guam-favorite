package waffle.guam.util

import com.github.benmanes.caffeine.cache.Caffeine
import java.time.Duration

class GuamCache<K, V> (
    maximumSize: Long,
    duration: Duration,
    private val loader: (K) -> V,
) {
    private val caffeineCache = Caffeine.newBuilder()
        .maximumSize(maximumSize)
        .expireAfterWrite(duration)
        .build<K, V>()

    fun get(key: K): V =
        caffeineCache.getIfPresent(key) ?: loader.invoke(key).also { value -> caffeineCache.put(key, value) }

    fun reload(key: K) = caffeineCache.put(key, loader.invoke(key))

    fun evict(key: K) = caffeineCache.invalidate(key)
}
