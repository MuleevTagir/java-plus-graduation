package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.practicum.HitRequestDto;
import ru.practicum.StatsResponseDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class StatsClientImpl implements StatsClient {
    private final DiscoveryClient discoveryClient;
    private RestClient restClient;

    @Autowired
    public StatsClientImpl(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Override
    public List<StatsResponseDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try {
            this.restClient = RestClient.create(this.getInstance().getUri().toString());
            var response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/stats")
                            .queryParam("start", start.format(formatter))
                            .queryParam("end", start.format(formatter))
                            .queryParam("uris", uris)
                            .queryParam("unique", unique)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<StatsResponseDto>>() {
                    });
            return response;
        } catch (Exception exp) {
            return Collections.emptyList();
        }
    }

    @Override
    public StatsResponseDto hit(HitRequestDto statDto) {
        try {
            this.restClient = RestClient.create(this.getInstance().getUri().toString());
            return restClient.post().uri("/hit").body(statDto).retrieve().body(StatsResponseDto.class);
        } catch (Exception exp) {
            log.info(exp.getMessage());
            return null;
        }
    }

    private ServiceInstance getInstance() {
        try {
            return discoveryClient
                    .getInstances("stats-server")
                    .getFirst();
        } catch (Exception exception) {
            throw new RuntimeException(
                    "Ошибка обнаружения адреса сервиса статистики с id: " + "stats-server",
                    exception
            );
        }
    }
}
