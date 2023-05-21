package otus.study.cashmachine.machine.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import otus.study.cashmachine.TestUtil;
import otus.study.cashmachine.bank.dao.CardsDao;
import otus.study.cashmachine.bank.data.Card;
import otus.study.cashmachine.bank.service.AccountService;
import otus.study.cashmachine.bank.service.impl.CardServiceImpl;
import otus.study.cashmachine.machine.data.CashMachine;
import otus.study.cashmachine.machine.data.MoneyBox;
import otus.study.cashmachine.machine.service.impl.CashMachineServiceImpl;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(MockitoExtension.class)
class CashMachineServiceTest {

    @Spy
    @InjectMocks
    private CardServiceImpl cardService;

    @Mock
    private CardsDao cardsDaoMock;

    @Mock
    private AccountService accountServiceMock;

    @Mock
    private MoneyBoxService moneyBoxService;

    private CashMachineServiceImpl cashMachineService;

    private CashMachine cashMachine = new CashMachine(new MoneyBox());

    @BeforeEach
    void init() {
        cashMachineService = new CashMachineServiceImpl(cardService, accountServiceMock, moneyBoxService);
    }


    @Test
    void getMoney() {
        String cardNum = "0";
        String pin = "1234";

        Mockito.doReturn(BigDecimal.ONE).when(cardService).getMoney(cardNum, pin, BigDecimal.ONE);
        Mockito.when(moneyBoxService.getMoney(any(), anyInt())).thenReturn(List.of(1, 2, 3, 4));

        List<Integer> res = cashMachineService.getMoney(cashMachine, cardNum, pin, BigDecimal.ONE);

        Assertions.assertEquals(List.of(1, 2, 3, 4), res);
    }

    @Test
    void putMoney() {
        String testCardNum = "1111";
        String testPin = "1234";
        long accId = 1L;
        BigDecimal sumToPut = BigDecimal.valueOf(5000 + 1000 + 500 + 100);

        Mockito.when(cardsDaoMock.getCardByNumber(testCardNum)).thenReturn(new Card(1, testCardNum, accId, TestUtil.getHash(testPin)));
        Mockito.when(accountServiceMock.putMoney(accId, sumToPut)).thenReturn(sumToPut);
        Mockito.when(cardService.getBalance(testCardNum, testPin)).thenReturn(BigDecimal.valueOf(0));

        BigDecimal res = cashMachineService.putMoney(cashMachine, testCardNum, testPin, List.of(1, 1, 1, 1));

        Assertions.assertEquals(sumToPut, res);
    }

    @Test
    void checkBalance() {
        String testCardNum = "1111";
        String testPin = "1234";
        long accId = 1L;

        Mockito.when(cardsDaoMock.getCardByNumber(testCardNum)).thenReturn(new Card(1, testCardNum, accId, TestUtil.getHash(testPin)));
        Mockito.when(accountServiceMock.checkBalance(accId)).thenReturn(BigDecimal.valueOf(42));

        BigDecimal res = cashMachineService.checkBalance(cashMachine, testCardNum, testPin);
        Assertions.assertEquals(BigDecimal.valueOf(42), res);
    }

    @Test
    void changePin() {
        String testCardNum = "1111";
        String oldPin = "1234";
        String newPin = "4321";

        Mockito.when(cardsDaoMock.getCardByNumber(testCardNum)).thenReturn(new Card(1, testCardNum, 1L, TestUtil.getHash(oldPin)));
        ArgumentCaptor<Card> capt = ArgumentCaptor.forClass(Card.class);

        cashMachineService.changePin(testCardNum, oldPin, newPin);
        Mockito.verify(cardsDaoMock).saveCard(capt.capture());

        Assertions.assertEquals(TestUtil.getHash(newPin), capt.getValue().getPinCode());
    }

    @Test
    void changePinWithAnswer() {
        String testCardNum = "1111";
        String oldPin = "1234";
        String newPin = "4321";

        Mockito.when(cardsDaoMock.getCardByNumber(testCardNum)).thenReturn(new Card(1, testCardNum, 1L, TestUtil.getHash(oldPin)));

        boolean res = cashMachineService.changePin(testCardNum, "incorrect_pin", newPin);
        Assertions.assertFalse(res);

        res = cashMachineService.changePin(testCardNum, oldPin, newPin);
        Assertions.assertTrue(res);
    }
}
