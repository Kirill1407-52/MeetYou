package com.kirill.meetyou.filters;

import com.kirill.meetyou.service.VisitCounterService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)  // Указываем порядок выполнения фильтра
public class VisitCounterFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(VisitCounterFilter.class);
    private final VisitCounterService visitCounterService;

    public VisitCounterFilter(VisitCounterService visitCounterService) {
        this.visitCounterService = visitCounterService;
    }

    @Override
    public void init(FilterConfig filterConfig) {
        log.info("VisitCounterFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String uri = httpRequest.getRequestURI();

        visitCounterService.increment(uri);
        log.debug("Incremented counter for: {}", uri);

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        log.info("VisitCounterFilter destroyed");
    }
}