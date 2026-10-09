package org.example.service.impl.visitor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@AllArgsConstructor
@Setter
@Getter
public class HeaderDTO {
    private LocalDateTime createdAt;
    private String procType;
    private LocalDateTime procTime;
}
