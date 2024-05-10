package ru.practicum.yandex.events.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.yandex.StatClient;
import ru.practicum.yandex.dto.EndpointHitDto;
import ru.practicum.yandex.dto.ViewStatsDto;
import ru.practicum.yandex.events.dto.EventFullDto;
import ru.practicum.yandex.events.dto.EventSearchFilter;
import ru.practicum.yandex.events.dto.EventShortDto;
import ru.practicum.yandex.events.mapper.EventMapper;
import ru.practicum.yandex.events.model.Event;
import ru.practicum.yandex.events.service.EventService;
import ru.practicum.yandex.shared.exception.IncorrectDateRangeException;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private static final String SERVICE_ID = "ewm-main-service";

    private final EventService eventService;

    private final EventMapper eventMapper;

    private final StatClient statClient;

    @GetMapping
    public List<EventShortDto> findEvents(EventSearchFilter searchFilter,
                                          @RequestParam(defaultValue = "0") Long from,
                                          @RequestParam(defaultValue = "10") Integer size,
                                          HttpServletRequest request) {
        log.info("Requesting events, search filter: '{}'.", searchFilter);
        validateDateRange(searchFilter);
        List<Event> events = eventService.findEvents(searchFilter, from, size);
        sendStatistics(request);
        return eventMapper.toShortDtoList(events);
    }

    @GetMapping("/{id}")
    public EventFullDto getFullEventInfoById(@PathVariable Long id,
                                             HttpServletRequest request) {
        log.info("Requesting full event info with id '{}'.", id);
        sendStatistics(request);
        ViewStatsDto statistic = getStatisticsWithUniqueIp(request);
        Long hits = statistic.getHits();
        Event event = eventService.getFullEventInfoById(id, hits);
        return eventMapper.toDto(event);
    }

    private void sendStatistics(HttpServletRequest request) {
        EndpointHitDto endpointHitDto = EndpointHitDto.builder()
                .app(SERVICE_ID)
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        statClient.methodHit(endpointHitDto);
    }

    private ViewStatsDto getStatisticsWithUniqueIp(HttpServletRequest request) {
        return statClient.getUniqueIpStatsForUri(request.getRequestURI());
    }

    private void validateDateRange(EventSearchFilter searchFilter) {
        if (searchFilter.getRangeStart() != null && searchFilter.getRangeEnd() != null) {
            if (searchFilter.getRangeStart().isAfter(searchFilter.getRangeEnd())) {
                throw new IncorrectDateRangeException("Wrong date range.");
            }
        }
    }
}
