import com.chaing.domain.inventories.dto.command.FactoryInboundCreateCommand;
import com.chaing.domain.inventories.exception.InventoriesErrorCode;
import com.chaing.domain.inventories.exception.InventoriesException;
import com.chaing.domain.inventories.usecase.executor.Executor;
import com.chaing.domain.inventories.usecase.reader.Reader;
import com.chaing.domain.inventories.usecase.valiator.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FactoryScanInboundServiceTests {

    @InjectMocks
    private FactoryInboundService factoryInboundService;

    @Mock
    @Qualifier("factory")
    private Reader reader;
    @Mock
    @Qualifier("factory")
    private Validator<FactoryInboundCreateCommand> validator;
    @Mock
    @Qualifier("factory")
    private Executor<FactoryInboundCreateCommand> executor;

    @Test
    @DisplayName("공장 제품 등록 성공 - 단건 시리얼 번호 검증 후 저장된다")
    void scanInboundItem_Success() {
        // given
        FactoryInboundCreateCommand command = new FactoryInboundCreateCommand(
                "SN-FACTORY-001", 1L, LocalDate.now()
        );

        // when
        factoryInboundService.scanInbound(command);

        // then
        verify(validator).checkAlreadyScanned(anyString()); // 단건 중복 체크 확인
        verify(validator).checkScanValidity(any(FactoryInboundCreateCommand.class));
        verify(executor).create(any(FactoryInboundCreateCommand.class));
    }

    @Test
    @DisplayName("공장 제품 등록 실패 - 시리얼 번호 형식이 틀리면 예외가 발생한다")
    void scanInboundItem_Fail_InvalidFormat() {
        // given
        FactoryInboundCreateCommand command = new FactoryInboundCreateCommand(
                "SHORT", 1L, LocalDate.now()
        );

        doThrow(new InventoriesException(InventoriesErrorCode.INVALID_SERIAL_CODE))
                .when(validator).checkScanValidity(any());

        // when & then
        assertThrows(InventoriesException.class, () -> {
            factoryInboundService.scanInbound(command);
        });

        verify(executor, never()).create(any());
    }
}