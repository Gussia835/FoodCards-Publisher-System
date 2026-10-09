package org.example.builders;

import org.example.models.gru.GruVistaTab;
import org.example.models.pom.PomUnit;
import org.example.models.pom.PomUnitError;
import org.example.service.impl.visitor.dto.BodyDTO;
import net.datafaker.Faker;
import org.example.utils.Constants;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;

@Component
public class EnrollBuilder {
    private final Faker faker = new Faker();

    /**
    Создает Entity для Header-строки файла.
     */
    public PomUnit buildPomUnitHeader(Long fileId, String line, boolean isValid) {
        return PomUnit.builder()
                .fileId(fileId)
                .pomType(Constants.POM_TYPE_HEADER)
                .status(isValid ? Constants.STATUS_SUCCESS : Constants.STATUS_ERROR)
                .unitValue(line)
                .addValue(isValid ? null : Constants.MSG_INVALID_HEADER)
                .build();
    }

    /**
     Создает Entity для Trailer-строки файла.
     */
    public PomUnit buildPomUnitTrailer(Long fileId, String line, boolean isValid) {
        return PomUnit.builder()
                .fileId(fileId)
                .pomType(Constants.POM_TYPE_TRAILER)
                .status(isValid ? Constants.STATUS_SUCCESS : Constants.STATUS_ERROR)
                .unitValue(line)
                .addValue(isValid ? null : Constants.MSG_INVALID_TRAILER)
                .build();
    }


    /**
     Создает Entity для Body-строки файла.
     */
    public PomUnit buildPomUnitBody(BodyDTO dto, Long fileId, String line, boolean isValid) {
        return PomUnit.builder()
                .fileId(fileId)
                .pomType(Constants.POM_TYPE_BODY)
                .status(isValid ? Constants.STATUS_SUCCESS : Constants.STATUS_ERROR)
                .unitValue(line)
                .addValue(isValid ? null : getErrorMessage(dto, isValid))
                .build();
    }

    /**
     Создает Entity для записи в таблицу GRU (бизнес-данные)
     */
    public GruVistaTab buildGru(BodyDTO dto, Long fileId, LocalDateTime procTime, String procType) {
        return GruVistaTab.builder()
                .systemAccount(dto.getAccount())
                .currency(Constants.DEFAULT_CURRENCY)
                .xalfa(new BigDecimal(dto.getAmount().trim()))
                .operation(dto.getOp_type())
                .pomId(null)
                .uterario(new BigDecimal(generateUterario()))
                .addInfo(getAddInfo(dto.getOp_type()))
                .fileId(fileId)
                .focStatus(Constants.STATUS_WAIT)
                .focTs(Constants.PROC_TYPE_IN_TIME.equals(procType) ? procTime : null)
                .focType(procType)
                .build();
    }

    /**
     Создает Entity для записи об ошибке валидации Body-строки.
     */
    public PomUnitError buildError(BodyDTO dto, Long fileId, String line, boolean isValid) {
        return PomUnitError.builder()
                .unitId(null)
                .errorSeq((short) 1)
                .errorCode(getErrorCode(dto, isValid))
                .errorField(line)
                .errorMsg(getErrorMessage(dto, isValid))
                .fileId(fileId)
                .build();
    }

    /**
     получение кода ошибки
     */
    private String getErrorCode(BodyDTO dto, boolean isValid) {
        if (!isValid
                || dto.getAccount().isEmpty()
                || !dto.getOp_type().matches(Constants.REGEX_VALID_OPERATIONS)) {
            return String.valueOf(HttpURLConnection.HTTP_BAD_REQUEST);
        }
        return String.valueOf(HttpURLConnection.HTTP_INTERNAL_ERROR);
    }

    /**
     получение сообщения об ошибке
     */
    private String getErrorMessage(BodyDTO dto, boolean isValid) {
        if (!isValid) {
            return Constants.MSG_FILE_TRAILER_HEADER_INVALID;
        }
        if (dto.getAccount().isEmpty()) {
            return Constants.MSG_ACCOUNT_EMPTY;
        }
        return Constants.MSG_INVALID_OP_TYPE + dto.getOp_type();
    }

    /**
        получение описания инфорамции
     */
    private String getAddInfo(String op) {
        return switch (op) {
            case Constants.OP_ZR -> Constants.DESC_ZR;
            case Constants.OP_CR -> Constants.DESC_CR;
            case Constants.OP_DR -> Constants.DESC_DR;
            default -> null;
        };
    }

    /**
     Получение 18-значного числа
     */
    private String generateUterario() {
        return faker.number().digits(18);
    }
}

