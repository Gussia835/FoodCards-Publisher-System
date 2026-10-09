package org.example.models.pom;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "UNIT_ERROR", schema = "POM")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PomUnitError {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pom_unit_error_seq_gen")
    @SequenceGenerator(name = "pom_unit_error_seq_gen", sequenceName = "POM_UNIT_ERROR_SEQ", schema = "POM", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "UNIT_ID", nullable = false)
    private Long unitId;

    @Column(name = "ERROR_SEQ", nullable = false)
    private Short errorSeq;

    @Column(name = "ERROR_CODE", length = 3)
    private String errorCode;

    @Column(name = "ERROR_FIELD", length = 2000)
    private String errorField;

    @Column(name = "ERROR_MSG", length = 1000)
    private String errorMsg;

    @Column(name = "FILE_ID", nullable = false)
    private Long fileId;
}