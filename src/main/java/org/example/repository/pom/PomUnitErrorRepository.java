package org.example.repository.pom;

import org.example.models.pom.PomUnitError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PomUnitErrorRepository extends JpaRepository<PomUnitError, Long> {


}
