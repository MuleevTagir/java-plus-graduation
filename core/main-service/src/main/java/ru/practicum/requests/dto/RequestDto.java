package ru.practicum.requests.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestDto {

    Long id;

    String status;

    Long event;

    Long requester;

    String created;

    List<RequestDto> confirmedRequests;

    List<RequestDto> rejectedRequests;
}