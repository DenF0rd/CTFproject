package com.example.util;

public class AppConfig {

    private static String baseUrl = "http://92.242.63.101:8080"; // URL вашего сервера

    static {
        String envUrl = System.getenv("APP_BASE_URL");
        if (envUrl != null && !envUrl.isEmpty()) {
            baseUrl = envUrl;
        }
        System.out.println("App base URL: " + baseUrl);
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static void setBaseUrl(String url) {
        baseUrl = url;
    }
}