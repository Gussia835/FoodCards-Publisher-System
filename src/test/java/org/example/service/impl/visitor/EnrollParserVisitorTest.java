package org.example.service.impl.visitor;

import org.example.builders.EnrollBuilder;
import org.example.models.gru.GruVistaTab;
import org.example.models.pom.PomUnit;
import org.example.models.pom.PomUnitError;
import org.example.service.dao.SaverDAO;
import org.example.service.impl.visitor.validator.EnrollValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollParserVisitorTest {
    @Mock
    private EnrollBuilder builder;
    @Mock
    private SaverDAO saver;
    @Mock
    private EnrollValidator validator;
    @InjectMocks
    private EnrollParserVisitor visitor;

    private static final String VALID_HEADER_LINE = "H 20260907 182606 IMMEDIATE               ";
    private static final String VALID_IN_TIME_HEADER = "H 20260907 182606 IN-TIME  20260811 235900";
    private static final String VALID_BODY_LINE = "Кузнецов Олег                                                                                       1000401000050003              DR                 777";
    private static final String INVALID_BODY_LINE = "Невалидная строка";
    private static final String VALID_TRAILER_LINE = "T                  1";

    @BeforeEach
    void setUp() {
        visitor.setContext(1L, true, true);
    }

    @Nested
    class HeaderParsingTests {
        @Test
        void validImmediateHeaderTest() {
            when(validator.isHeaderLine(VALID_HEADER_LINE)).thenReturn(true);
            when(builder.buildPomUnitHeader(any(), any(), anyBoolean())).thenReturn(new PomUnit());
            when(saver.savePomUnit(any())).thenReturn(new PomUnit());

            boolean result = visitor.visit(VALID_HEADER_LINE);
            assertThat(result).isTrue();
            verify(saver, times(1)).savePomUnit(any());
            verify(saver, never()).saveGRU(any());
            verify(saver, never()).saveError(any());
        }

        @Test
        void validInTimeHeaderTest() {
            when(validator.isHeaderLine(VALID_IN_TIME_HEADER)).thenReturn(true);
            when(builder.buildPomUnitHeader(any(), any(), anyBoolean())).thenReturn(new PomUnit());
            when(saver.savePomUnit(any())).thenReturn(new PomUnit());

            boolean result = visitor.visit(VALID_IN_TIME_HEADER);
            assertThat(result).isTrue();
            verify(saver, times(1)).savePomUnit(any());


        }
    }

    @Nested
    class BodyParsingTests {
        @Test
        void validBodyTest() {
            when(validator.isHeaderLine(VALID_BODY_LINE)).thenReturn(false);
            when(validator.isTrailerLine(VALID_BODY_LINE)).thenReturn(false);
            when(validator.validateBody(VALID_BODY_LINE)).thenReturn(true);

            PomUnit mockUnit = new PomUnit();
            mockUnit.setId(100L);
            when(builder.buildPomUnitBody(any(), any(), any(), anyBoolean())).thenReturn(mockUnit);
            when(saver.savePomUnit(any())).thenReturn(mockUnit);
            when(builder.buildGru(any(), any(), any(), any())).thenReturn(new GruVistaTab());

            boolean result = visitor.visit(VALID_BODY_LINE);
            assertThat(result).isTrue();
            verify(saver, times(1)).savePomUnit(any());
            verify(saver, times(1)).saveGRU(any());
            verify(saver, never()).saveError(any());


        }

        @Test
        void invalidBodyTest() {
            when(validator.isHeaderLine(INVALID_BODY_LINE)).thenReturn(false);
            when(validator.isTrailerLine(INVALID_BODY_LINE)).thenReturn(false);
            when(validator.validateBody(INVALID_BODY_LINE)).thenReturn(false);

            PomUnit mockUnit = new PomUnit();
            mockUnit.setId(101L);
            when(builder.buildPomUnitBody(any(), any(), any(), anyBoolean())).thenReturn(mockUnit);
            when(saver.savePomUnit(any())).thenReturn(mockUnit);
            when(builder.buildError(any(), any(), any(), anyBoolean())).thenReturn(new PomUnitError());

            boolean result = visitor.visit(INVALID_BODY_LINE);
            assertThat(result).isTrue();
            verify(saver, times(1)).savePomUnit(any());
            verify(saver, never()).saveGRU(any());
            verify(saver, times(1)).saveError(any());
        }

        @Test
        void validBodyWithInvalidHeaderContextTest() {
            visitor.setContext(1L, false, true);
            when(validator.isHeaderLine(VALID_BODY_LINE)).thenReturn(false);
            when(validator.isTrailerLine(VALID_BODY_LINE)).thenReturn(false);

            PomUnit mockUnit = new PomUnit();
            when(builder.buildPomUnitBody(any(), any(), any(), anyBoolean())).thenReturn(mockUnit);
            when(saver.savePomUnit(any())).thenReturn(mockUnit);
            when(builder.buildError(any(), any(), any(), anyBoolean())).thenReturn(new PomUnitError());

            boolean result = visitor.visit(VALID_BODY_LINE);
            assertThat(result).isTrue();
            verify(saver, times(1)).savePomUnit(any());
            verify(saver, never()).saveGRU(any());
            verify(saver, times(1)).saveError(any()); // Добавлено для полноты проверки
        }
    }



    @Nested
    class TrailerParsingTests {
        @Test
        void validTrailerTest() {
            when(validator.isTrailerLine(VALID_TRAILER_LINE)).thenReturn(true);
            when(builder.buildPomUnitTrailer(any(), any(), anyBoolean())).thenReturn(new PomUnit());
            when(saver.savePomUnit(any())).thenReturn(new PomUnit());

            boolean result = visitor.visit(VALID_TRAILER_LINE);
            assertThat(result).isTrue();
            verify(saver, times(1)).savePomUnit(any());
            verify(saver, never()).saveGRU(any());
            verify(saver, never()).saveError(any());
        }
    }
}