package org.example.service.dao;

import lombok.RequiredArgsConstructor;
import org.example.exception.DatabaseSaveException;
import org.example.models.gru.GruVistaTab;
import org.example.models.pom.PomFile;
import org.example.models.pom.PomUnit;
import org.example.models.pom.PomUnitError;
import org.example.repository.gru.GruVistaTabRepository;
import org.example.repository.pom.PomFileRepository;
import org.example.repository.pom.PomUnitErrorRepository;
import org.example.repository.pom.PomUnitRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SaverDAO {
    private final PomUnitRepository pomUnitRepository;
    private final PomUnitErrorRepository pomUnitErrorRepository;
    private final PomFileRepository pomFileRepository;
    private final GruVistaTabRepository gruVistaTabRepository;

    @Transactional("pomTransactionManager")
    public PomFile savePomFile(PomFile pomFile) {
        try {
            return pomFileRepository.save(pomFile);
        } catch (DataAccessException e) {
            throw new DatabaseSaveException("PomFile", e);
        }
    }

    @Transactional("gruTransactionManager")
    public void saveGRU(GruVistaTab gru) {
        try {
            gruVistaTabRepository.save(gru);
        } catch (DataAccessException e) {
            throw new DatabaseSaveException("GruVistaTab", e);
        }
    }

    @Transactional("pomTransactionManager")
    public void saveError(PomUnitError error) {
        try {
            pomUnitErrorRepository.save(error);
        } catch (DataAccessException e) {
            throw new DatabaseSaveException("PomUnitError", e);
        }
    }

    @Transactional("pomTransactionManager")
    public PomUnit savePomUnit(PomUnit pomUnit) {
        try {
            return pomUnitRepository.saveAndFlush(pomUnit);
        } catch (DataAccessException e) {
            throw new DatabaseSaveException("PomUnit", e);
        }
    }
}
