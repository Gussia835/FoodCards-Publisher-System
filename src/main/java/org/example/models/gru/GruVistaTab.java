package org.example.models.gru;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "GRU_VISTA_TAB", schema = "GRU")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GruVistaTab {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
                    generator = "gru_vista_seq")
    @SequenceGenerator(name = "gru_vista_seq",
                        schema = "GRU",
                        sequenceName = "GRU_VISTA_TAB_SEQ",
                        allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "SYSTEMACCOUNT", nullable = false, length = 32)
    private String systemAccount;

    @Column(name = "CURRENCY", nullable = false, length = 3)
    @Builder.Default
    private String currency = "222";

    @Column(name = "XALFA", precision = 23, scale = 3)
    private BigDecimal xalfa;

    @Column(name = "OPERATION", nullable = false, length = 3)
    private String operation;

    @CreationTimestamp
    @Column(name = "TIME_STAMP", insertable = false, updatable = false)
    private LocalDateTime timeStamp;

    @Column(name = "POM_ID", nullable = false)
    private Long pomId;

    @Column(name = "UTERRARIO", nullable = false, length = 18)
    private BigDecimal uterario;

    @Column(name = "OLDTBAL")
    private BigDecimal oldTbal;

    @Column(name = "NEWTBAL")
    private BigDecimal newTbal;

    @Column(name = "ADD_INFO", length = 100)
    private String addInfo;

    @Column(name = "FILE_ID", nullable = false)
    private Long fileId;

    @Column(name = "FOC_STATUS", nullable = false, length = 64)
    @Builder.Default
    private String focStatus = "WAIT";

    @Column(name = "FOC_TS")
    private LocalDateTime focTs;

    @Column(name = "FOC_TYPE", length = 10)
    private String focType;
}