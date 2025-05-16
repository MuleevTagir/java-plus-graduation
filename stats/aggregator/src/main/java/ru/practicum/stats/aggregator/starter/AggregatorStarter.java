package ru.practicum.stats.aggregator.starter;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.aggregator.handler.UserActionHandler;

import java.time.Duration;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Component
@Slf4j
public class AggregatorStarter implements Runnable {
    final Consumer<String, UserActionAvro> consumer;
    final UserActionHandler handler;

    @Override
    public void run() {
        try {
            log.info("Получение данных");
            while (true) {
                ConsumerRecords<String, UserActionAvro> records = consumer.poll(Duration.ofMillis(500));

                for (ConsumerRecord<String, UserActionAvro> record : records) {
                    handler.handle(record.value());
                    handler.flush();
                    consumer.commitAsync();
                }
            }
        } catch (WakeupException e) {

        } catch (Exception e) {
            log.error("Сбой обработки ", e);
            log.error(e.getMessage());
        } finally {
            try {
                handler.flush();
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                handler.close();
            }
        }
    }

    @PostConstruct
    public void init() {
        Thread thread = new Thread(this);
        thread.setName("aggregator");
        thread.start();
    }
}
