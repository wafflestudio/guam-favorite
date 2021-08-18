package waffle.guam

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("!test")
@Aspect
@Component
class EventPublisher(
    private val eventPublisher: ApplicationEventPublisher
) {

    @Pointcut("within(waffle.guam..*ServiceImpl)")
    fun calledInService() {}

    @Around("calledInService()")
    fun doPublishEvent(jp: ProceedingJoinPoint): Any? {
        val result = jp.proceed()

        if (result is GuamEvent) {
            eventPublisher.publishEvent(result)
        }

        return result
    }
}
