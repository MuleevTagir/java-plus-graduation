package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.StatsResponseDto;
import ru.practicum.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatRepository extends JpaRepository<Hit, Long> {

    @Query("select new ru.practicum.StatsResponseDto(h.app, h.uri, COUNT(h.ip)) " +
            "      from Hit as h" +
            "      where h.timestamp between ?1 and ?2" +
            "      GROUP BY h.app, h.uri" +
            " ORDER BY COUNT(h.ip) DESC")
    List<StatsResponseDto> findAllTimestampBetweenStartAndEnd(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.StatsResponseDto(h.app, h.uri, COUNT(h.ip)) " +
            " FROM Hit as h" +
            " WHERE h.uri IN ?1" +
            " GROUP BY h.app, h.uri" +
            " order by COUNT(h.ip) desc")
    List<StatsResponseDto> getStatsByUri(List<String> uris, LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.StatsResponseDto(h.app, h.uri, COUNT(DISTINCT h.ip)) " +
            " FROM Hit as h" +
            " WHERE h.uri IN ?1 AND h.timestamp BETWEEN ?2 AND ?3" +
            " GROUP BY h.app, h.uri" +
            " ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<StatsResponseDto> getStatsByUriWithUniqueIps(List<String> uris, LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.StatsResponseDto(h.app, h.uri, COUNT(DISTINCT h.ip)) " +
            " from Hit as h" +
            " where h.timestamp > ?1 and h.timestamp < ?2 " +
            " group by h.app, h.uri" +
            " order by COUNT(DISTINCT h.ip) desc")
    List<StatsResponseDto> getStatsByTimeWithUniqueIps(LocalDateTime start, LocalDateTime end);
}
