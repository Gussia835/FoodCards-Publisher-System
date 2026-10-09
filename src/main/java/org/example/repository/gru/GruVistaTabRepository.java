package org.example.repository.gru;

import org.example.models.gru.GruVistaTab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GruVistaTabRepository extends JpaRepository<GruVistaTab, Long> {

}
