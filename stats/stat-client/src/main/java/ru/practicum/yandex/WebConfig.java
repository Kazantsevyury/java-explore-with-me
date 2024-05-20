package ru.practicum.yandex;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Конфигурационный класс для настройки WebClient.
 */
@Configuration
public class WebConfig {

    /**
     * Создает и настраивает экземпляр WebClient.
     *
     * @param addressBaseUrl базовый URL сервера статистики, получаемый из настроек приложения
     * @return настроенный WebClient
     */
    @Bean
    public WebClient webClient(@Value("${stat-server.url}") String addressBaseUrl) {
        WebClient webClient = WebClient.create(addressBaseUrl);
        return webClient.mutate()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
