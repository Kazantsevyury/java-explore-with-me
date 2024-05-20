package ru.practicum.yandex;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.yandex.dto.EndpointHitDto;
import ru.practicum.yandex.dto.ViewStatsDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Реализация клиента для взаимодействия с сервисом статистики.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class StatClientImpl implements StatClient {

    private final WebClient webClient;

    /**
     * Отправляет информацию о посещении эндпоинта.
     *
     * @param endpointHitDto данные о посещении
     * @return данные о посещении, сохраненные в сервисе статистики
     */
    @Override
    public EndpointHitDto methodHit(EndpointHitDto endpointHitDto) {
        String uri = "/hit";
        log.info("Запрос StatClient на uri '{}'. Тело '{}'.", uri, endpointHitDto);
        EndpointHitDto response = webClient
                .post()
                .uri(uri)
                .bodyValue(endpointHitDto)
                .retrieve()
                .bodyToMono(EndpointHitDto.class)
                .block();
        log.info("Ответ StatClient от uri '{}'. Тело ответа '{}'.", uri, response);
        return response;
    }

    /**
     * Получает статистику посещений за указанный период.
     *
     * @param start  начало периода
     * @param end    конец периода
     * @param uris   список URI для фильтрации
     * @param unique учитывать только уникальные посещения
     * @return список статистических данных
     */
    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        String requestUrl = String.format("/stats?start=%s&end=%s&uris=%s&unique=%s",
                URLEncoder.encode(String.valueOf(start), StandardCharsets.UTF_8),
                URLEncoder.encode(String.valueOf(end), StandardCharsets.UTF_8),
                String.join(",", uris),
                unique);

        log.info("Запрос StatClient на uri '{}'.", requestUrl);
        List<ViewStatsDto> response = webClient.get()
                .uri(requestUrl)
                .retrieve()
                .bodyToFlux(ViewStatsDto.class)
                .collectList()
                .block();
        log.info("Ответ StatClient от uri '{}'. Тело ответа '{}'.", requestUrl, response);
        return response;
    }

    /**
     * Получает уникальную статистику по конкретному URI.
     *
     * @param uri URI для получения статистики
     * @return данные статистики
     */
    @Override
    public ViewStatsDto getUniqueIpStatsForUri(String uri) {
        log.info("Запрос StatClient на уникальные статистические данные по uri '{}'.", uri);
        ViewStatsDto response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/statistic")
                        .queryParam("uri", uri)
                        .build())
                .retrieve()
                .bodyToMono(ViewStatsDto.class)
                .block();
        log.info("Ответ StatClient от уникальных статистических данных по uri '{}'. Тело ответа '{}'.", uri, response);
        return response;
    }
}
