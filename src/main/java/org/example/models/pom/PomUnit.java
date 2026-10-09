package org.example.models.pom;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "UNIT", schema = "POM")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PomUnit {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pom_unit_seq_gen")
    @SequenceGenerator(name = "pom_unit_seq_gen", sequenceName = "POM_UNIT_SEQ", schema = "POM", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "FILE_ID", nullable = false)
    private Long fileId;

    @CreationTimestamp
    @Column(name = "INS_TIME", insertable = false, updatable = false)
    private LocalDateTime insTime;

    @Column(name = "POM_TYPE", nullable = false, length = 3)
    private String pomType;

    @Column(name = "STATUS", nullable = false, length = 10)
    private String status;

    @Column(name = "UNIT_VALUE", length = 2000)
    private String unitValue;

    @UpdateTimestamp
    @Column(name = "UPD_TIME")
    private LocalDateTime updTime;

    @Column(name = "ADD_VALUE", length = 100)
    private String addValue;
}