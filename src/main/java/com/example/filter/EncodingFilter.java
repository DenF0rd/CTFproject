package com.example.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class EncodingFilter implements Filter {

    private static final String ENCODING = "UTF-8";
    private String encoding = ENCODING;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String configEncoding = filterConfig.getInitParameter("encoding");
        if (configEncoding != null && !configEncoding.isEmpty()) {
            encoding = configEncoding;
        }
        System.out.println("EncodingFilter initialized with " + encoding + " encoding");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        httpRequest.setCharacterEncoding(encoding);
        httpResponse.setCharacterEncoding(encoding);
        httpResponse.setContentType("text/html; charset=" + encoding);
        httpResponse.setHeader("Content-Type", "text/html; charset=" + encoding);

        chain.doFilter(httpRequest, httpResponse);
    }

    @Override
    public void destroy() {
        // Очистка
    }
}