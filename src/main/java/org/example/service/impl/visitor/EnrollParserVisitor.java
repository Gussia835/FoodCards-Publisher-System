package org.example.service.impl.visitor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.builders.EnrollBuilder;
import org.example.models.gru.GruVistaTab;
import org.example.models.pom.PomUnit;
import org.example.models.pom.PomUnitError;
import org.example.service.impl.visitor.dto.BodyDTO;
import org.example.service.impl.visitor.dto.HeaderDTO;
import org.example.service.impl.visitor.dto.TrailerDTO;
import org.example.service.dao.SaverDAO;
import org.example.service.impl.visitor.validator.EnrollValidator;
import org.example.utils.Constants;
import org.example.service.impl.visitor.interfces.ParserVisitor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnrollParserVisitor implements ParserVisitor {

    private final EnrollBuilder builder;
    private final SaverDAO saver;
    private final EnrollValidator validator;
    private Long fileId;
    private LocalDateTime procTime;
    private String procType;
    private boolean isValidHeaderTrailer = true;

    /**
     * Начальная ифнормация для обработки файла
     *
     * @param fileId - id обрабатываемого файла
     * @param isValidHeader - валидный ли заголовок для задания статуса файла
     * @param isValidTrailer - валидный ли трейлер для задания статуса файла
     */

    public void setContext(Long fileId, boolean isValidHeader, boolean isValidTrailer) {
        this.fileId = fileId;
        this.isValidHeaderTrailer = isValidHeader && isValidTrailer;
    }

    /**
     * Обрабатывает строку файла в зависимости от её типа
    */
    @Override
    public boolean visit(String line) {
        if (validator.isHeaderLine(line)) {
            log.debug("parse & save valid header: {}", line);
            HeaderDTO headerDTO = parseHeader(line);
            procTime = headerDTO.getProcTime();
            procType = headerDTO.getProcType();
            PomUnit headerEntity = builder.buildPomUnitHeader(fileId, line, isValidHeaderTrailer);
            saver.savePomUnit(headerEntity);

            return true;
        }

        if (validator.isTrailerLine(line)) {
            log.debug("parse & save valid trailer: {}", line);
            PomUnit trailerEntity = builder.buildPomUnitTrailer(fileId, line, isValidHeaderTrailer);
            saver.savePomUnit(trailerEntity);

            return true;
        }

        log.debug("parse & save body: {}", line);
        BodyDTO bodyDTO = parseBody(line);
        boolean isValidBody = isValidHeaderTrailer
                                && validator.validateBody(line);
        log.debug("body is valid: {}", isValidBody);
        PomUnit bodyEntity = builder.buildPomUnitBody(bodyDTO, fileId, line, isValidBody);
        PomUnit savedUnit = saver.savePomUnit(bodyEntity);

        if (isValidBody) {
            log.debug("valid body {}", line);
            GruVistaTab gru = builder.buildGru(bodyDTO, fileId, procTime, procType);
            gru.setPomId(savedUnit.getId());
            saver.saveGRU(gru);
        } else {
            log.error("invalid body {}", line);
            PomUnitError error = builder.buildError(bodyDTO, fileId, line, isValidBody);
            error.setUnitId(savedUnit.getId());
            saver.saveError(error);
        }

        return true;
    }

    /**
     * Парсит строку заголовка файла
     *
     * @param line Строка заголовка
     * @return DTO с распарсенными данными заголовка
     */
    private HeaderDTO parseHeader(String line) {
        String createdStr = safeSubstring(line, Constants.IDX_CREATED_START, Constants.IDX_CREATED_END);
        LocalDateTime createdAt = LocalDateTime.parse(createdStr, Constants.DT_FORMATTER);
        String lineProcType = safeSubstring(line, Constants.IDX_PROC_START, Constants.IDX_PROC_END);
        String processTimeStr = safeSubstring(line, Constants.IDX_PROCESS_TIME_START, Constants.IDX_PROCESS_TIME_END);

        LocalDateTime lineProcTime = null;
        if (org.apache.commons.lang3.StringUtils.isNotBlank(processTimeStr)) {
            procTime = LocalDateTime.parse(processTimeStr, Constants.DT_FORMATTER);
        }

        return new HeaderDTO(createdAt, lineProcType, lineProcTime);
    }

    /**
     * Парсит строку body
     *
     * @param line Строка данных
     * @return DTO с распарсенными данными операции
     */
    private BodyDTO parseBody(String line) {
        String fio = safeSubstring(line, Constants.IDX_FIO_START, Constants.IDX_FIO_END);
        String account = safeSubstring(line, Constants.IDX_ACC_START, Constants.IDX_ACC_END);
        String typeOp = safeSubstring(line, Constants.IDX_TYPE_START, Constants.IDX_TYPE_END);
        String summ = safeSubstring(line, Constants.IDX_SUMM_START, Constants.IDX_SUMM_END);

        return new BodyDTO(fio, account, typeOp, summ);
    }

    /**
    * Безопасное извлечение подстроки с проверкой границ.
    */
    private String safeSubstring(String line, int start, int end) {
        if (line == null || line.length() <= start) {
            return "";
        }
        int actualEnd = Math.min(line.length(), end);
        return line.substring(start, actualEnd).trim();
    }
}
