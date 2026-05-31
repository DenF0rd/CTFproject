package com.example.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Фильтр для установки кодировки UTF-8*/

@WebFilter("/*")
public class EncodingFilter implements Filter {

    private static final String ENCODING = "UTF-8";
    private String encoding = ENCODING;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Можно переопределить кодировку через параметры web.xml
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

        // Устанавливаем кодировку для запроса
        httpRequest.setCharacterEncoding(encoding);

        // Устанавливаем кодировку для ответа
        httpResponse.setCharacterEncoding(encoding);

        // Устанавливаем Content-Type для HTML страниц
        httpResponse.setContentType("text/html; charset=" + encoding);

        // Добавляем заголовок для браузера
        httpResponse.setHeader("Content-Type", "text/html; charset=" + encoding);

        // Продолжаем обработку
        chain.doFilter(httpRequest, httpResponse);
    }

    @Override
    public void destroy() {
        // Очистка
    }
}