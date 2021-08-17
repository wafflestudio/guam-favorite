package waffle.guam.config

import org.slf4j.MDC
import org.springframework.stereotype.Component
import java.util.UUID
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

@Component
class RequestFilter : Filter {
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse?, filterChain: FilterChain) {
        val uniqueId = UUID.randomUUID()
        MDC.put("request-id", uniqueId.toString())
        filterChain.doFilter(servletRequest, servletResponse)
    }
}
