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

@Component
@Slf4j
@RequiredArgsConstructor
public class StatClient {

    private final WebClient webClient;

    public EndpointHitDto methodHit(EndpointHitDto endpointHitDto) {
        String uri = "/hits";
        log.info("StatClient request on uri '{}'. Body '{}'.", uri, endpointHitDto);
        return webClient
                .post()
                .uri(uri)
                .bodyValue(endpointHitDto)
                .retrieve()
                .bodyToMono(EndpointHitDto.class)
                .block();
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        String requestUrl = String.format("/stats?start=%s&end=%s&uris=%s&unique=%s",
                URLEncoder.encode(String.valueOf(start), StandardCharsets.UTF_8),
                URLEncoder.encode(String.valueOf(end), StandardCharsets.UTF_8),
                uris,
                unique);

        log.info("StatClient request on uri '{}'.", requestUrl);
        return webClient.get()
                .uri(requestUrl)
                .retrieve()
                .bodyToFlux(ViewStatsDto.class)
                .collectList()
                .block();
    }
}
