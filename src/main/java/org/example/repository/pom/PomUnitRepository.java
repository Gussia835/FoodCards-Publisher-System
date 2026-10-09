package org.example.repository.pom;

import org.example.models.pom.PomUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PomUnitRepository extends JpaRepository<PomUnit, Long> {



}
