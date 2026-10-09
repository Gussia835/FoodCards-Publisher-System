package org.example.service.impl.visitor.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class BodyDTO {
    private String fio;
    private String account;
    private String op_type;
    private String amount;
}
