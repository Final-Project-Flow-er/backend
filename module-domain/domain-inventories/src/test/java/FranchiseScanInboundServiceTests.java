//import com.chaing.domain.inventories.dto.command.FranchiseInboundCreateCommand;
//import com.chaing.domain.inventories.exception.InventoriesErrorCode;
//import com.chaing.domain.inventories.exception.InventoriesException;
//import com.chaing.domain.inventories.usecase.executor.Executor;
//import com.chaing.domain.inventories.usecase.reader.Reader;
//import com.chaing.domain.inventories.usecase.valiator.Validator;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.beans.factory.annotation.Qualifier;
//
//import java.time.LocalDate;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.doThrow;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//
//@ExtendWith(MockitoExtension.class)
//class FranchiseScanInboundServiceTests {
//
//    @InjectMocks
//    private FranchiseInboundService franchiseInboundService;
//
//    @Mock
//    @Qualifier("franchise")
//    private Reader reader;
//    @Mock
//    @Qualifier("franchise")
//    private Validator<FranchiseInboundCreateCommand> validator;
//    @Mock
//    @Qualifier("franchise")
//    private Executor<FranchiseInboundCreateCommand> executor;
//
//    @Test
//    @DisplayName("가맹점 입고 성공 - 모든 검증을 통과하면 저장이 실행된다")
//    void scanInboundBox_Success() {
//        // given
//        FranchiseInboundCreateCommand command = new FranchiseInboundCreateCommand(
//                "BOX-001", List.of("SN-001", "SN-002"), 1L, LocalDate.now(), 10L
//        );
//
//        // when
//        franchiseInboundService.scanInbound(command);
//
//        // then
//        verify(validator).checkAlreadyScanned(anyString()); // 리스트 중복 체크 확인
//        verify(validator).checkScanValidity(any(FranchiseInboundCreateCommand.class));
//        verify(executor).create(any(FranchiseInboundCreateCommand.class));
//    }
//
//    @Test
//    @DisplayName("가맹점 입고 실패 - 이미 스캔된 번호가 있으면 예외가 발생한다")
//    void scanInboundBox_Fail_AlreadyScanned() {
//        // given
//        FranchiseInboundCreateCommand command = new FranchiseInboundCreateCommand(
//                "BOX-001", List.of("SN-001"), 1L, LocalDate.now(), 10L
//        );
//
//        // validator가 에러를 던지도록 설정
//        doThrow(new InventoriesException(InventoriesErrorCode.INVENTORIES_ALREADY_EXISTS))
//                .when(validator).checkAlreadyScanned(anyString());
//
//        // when & then
//        assertThrows(InventoriesException.class, () -> {
//            franchiseInboundService.scanInbound(command);
//        });
//
//        // 저장 로직은 실행되지 않아야 함
//        verify(executor, never()).create(any());
//    }
//}