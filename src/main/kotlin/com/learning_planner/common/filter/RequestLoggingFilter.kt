package com.learning_planner.common.filter

import groovy.util.logging.Slf4j
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Slf4j
class RequestLoggingFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestURL = request.requestURL.toString()
        val queryString = request.queryString
        val method = request.method

        // 쿼리 스트링이 있는 경우 URL에 추가
        val fullURL = if (queryString.isNullOrEmpty()) {
            requestURL
        } else {
            "$requestURL?$queryString"
        }

        log.info(">>> Request URL: [$method] $fullURL")

        try {
            filterChain.doFilter(request, response)
        } finally {
            log.info("<<< Response Status: ${response.status}")
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}

// application.yml 또는 application.properties 설정
// logging:
//   level:
//     your.package.name: INFO  # 로깅 레벨 설정