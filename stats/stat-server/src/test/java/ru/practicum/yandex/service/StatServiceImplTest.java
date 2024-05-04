package ru.practicum.yandex.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.yandex.model.EndpointHit;
import ru.practicum.yandex.model.ViewStats;
import ru.practicum.yandex.repository.StatRepository;
import static org.mockito.Mockito.times;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StatServiceImplTest {

    @Mock
    private StatRepository statRepository;

    @InjectMocks
    private StatServiceImpl statService;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<String> uris;
    private List<ViewStats> viewStatsList;

    @BeforeEach
    public void setUp() {
        startDate = LocalDateTime.now().minusDays(1);
        endDate = LocalDateTime.now();
        uris = Arrays.asList("/test1", "/test2");
        viewStatsList = Arrays.asList(new ViewStats("app1", "/test1", 100L), new ViewStats("app2", "/test2", 200L));
    }

    @Test
    public void testViewStatsWhenUrisIsNullAndUniqueIsTrueThenReturnListOfViewStats() {
        when(statRepository.findStatsWithUniqueIps(startDate, endDate)).thenReturn(viewStatsList);

        List<ViewStats> result = statService.viewStats(startDate, endDate, null, true);

        assertThat(result).isEqualTo(viewStatsList);
    }

    @Test
    public void testViewStatsWhenUrisIsNotNullAndUniqueIsFalseThenReturnListOfViewStats() {
        when(statRepository.findStatsFromUrlList(startDate, endDate, uris)).thenReturn(viewStatsList);

        List<ViewStats> result = statService.viewStats(startDate, endDate, uris, false);

        assertThat(result).isEqualTo(viewStatsList);
    }

    @Test
    public void testViewStatsWhenUrisIsEmptyAndUniqueIsTrueThenReturnListOfViewStats() {
        when(statRepository.findStatsWithUniqueIps(startDate, endDate)).thenReturn(viewStatsList);

        List<ViewStats> result = statService.viewStats(startDate, endDate, Collections.emptyList(), true);

        assertThat(result).isEqualTo(viewStatsList);
    }

    @Test
    public void testViewStatsWhenUrisIsNotEmptyAndUniqueIsTrueThenReturnListOfViewStats() {
        when(statRepository.findStatsFromListWithUniqueIps(startDate, endDate, uris)).thenReturn(viewStatsList);

        List<ViewStats> result = statService.viewStats(startDate, endDate, uris, true);

        assertThat(result).isEqualTo(viewStatsList);
    }

    @Test
    public void testViewStatsWhenUrisIsNullAndUniqueIsFalseThenReturnListOfViewStats() {
        when(statRepository.findStats(startDate, endDate)).thenReturn(viewStatsList);

        List<ViewStats> result = statService.viewStats(startDate, endDate, null, false);

        assertThat(result).isEqualTo(viewStatsList);
    }

    @Test
    public void testViewStatsWhenUrisIsNotNullAndUniqueIsTrueThenReturnListOfViewStats() {
        when(statRepository.findStatsFromListWithUniqueIps(startDate, endDate, uris)).thenReturn(viewStatsList);

        List<ViewStats> result = statService.viewStats(startDate, endDate, uris, true);

        assertThat(result).isEqualTo(viewStatsList);
    }

    @Test
    public void testViewStatsWhenUrisIsEmptyAndUniqueIsFalseThenReturnListOfViewStats() {
        when(statRepository.findStats(startDate, endDate)).thenReturn(viewStatsList);

        List<ViewStats> result = statService.viewStats(startDate, endDate, Collections.emptyList(), false);

        assertThat(result).isEqualTo(viewStatsList);
    }

    @Test
    public void testViewStatsWhenUrisIsNotEmptyAndUniqueIsFalseThenReturnListOfViewStats() {
        when(statRepository.findStatsFromUrlList(startDate, endDate, uris)).thenReturn(viewStatsList);

        List<ViewStats> result = statService.viewStats(startDate, endDate, uris, false);

        assertThat(result).isEqualTo(viewStatsList);
    }

    @Test
    public void testMethodHitWhenEndpointHitThenSavedToRepository() {
        // Arrange
        EndpointHit endpointHit = new EndpointHit();
        when(statRepository.save(endpointHit)).thenReturn(endpointHit);

        // Act
        EndpointHit result = statService.methodHit(endpointHit);

        // Assert
        assertEquals(endpointHit, result);
        verify(statRepository, times(1)).save(endpointHit);
    }

}