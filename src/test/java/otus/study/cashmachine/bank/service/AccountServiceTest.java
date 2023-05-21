package otus.study.cashmachine.bank.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import otus.study.cashmachine.bank.dao.AccountDao;
import otus.study.cashmachine.bank.data.Account;
import otus.study.cashmachine.bank.service.impl.AccountServiceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    AccountDao accountDaoMock;

    @InjectMocks
    AccountServiceImpl accountServiceImpl;


    long testAccId = 0L;
    BigDecimal testAccAmount = BigDecimal.valueOf(84);

    Account testAcc;

    @BeforeEach
    void init() {
        testAcc = new Account(testAccId, testAccAmount);
    }

    @Test
    void createAccountMock() {
        ArgumentMatcher<Account> matcher =
                acc -> Objects.equals(acc.getAmount(), testAcc.getAmount()) && acc.getId() == testAcc.getId();

        Mockito.when(accountDaoMock.saveAccount(argThat(matcher))).thenReturn(testAcc);
        Account actualAcc = accountServiceImpl.createAccount(testAccAmount);
        Assertions.assertEquals(testAcc, actualAcc);
    }

    @Test
    void createAccountCaptor() {
        ArgumentCaptor<Account> accCaptor = ArgumentCaptor.forClass(Account.class);

        accountServiceImpl.createAccount(BigDecimal.valueOf(42));
        accountServiceImpl.createAccount(BigDecimal.valueOf(43));
        Mockito.verify(accountDaoMock, Mockito.times(2)).saveAccount(accCaptor.capture());

        List<BigDecimal> savedAmounts = accCaptor.getAllValues().stream()
                .map(Account::getAmount).toList();

        Assertions.assertEquals(List.of(BigDecimal.valueOf(42), BigDecimal.valueOf(43)), savedAmounts);
    }

    @Test
    void addSum() {
        Mockito.when(accountDaoMock.getAccount(testAccId)).thenReturn(testAcc);

        BigDecimal amountToAdd = BigDecimal.valueOf(42);
        accountServiceImpl.putMoney(testAccId, amountToAdd);
        Assertions.assertEquals(testAccAmount.add(amountToAdd), testAcc.getAmount());
    }

    @Test
    void getSum() {
        long accId = 1L;
        Account testAcc = new Account(accId, BigDecimal.valueOf(452));
        Mockito.when(accountDaoMock.getAccount(accId)).thenReturn(testAcc);

        BigDecimal newBalance = accountServiceImpl.getMoney(accId, BigDecimal.valueOf(451));
        Assertions.assertEquals(BigDecimal.ONE, newBalance);
        Assertions.assertEquals(BigDecimal.ONE, accountServiceImpl.checkBalance(accId));

        Exception e = assertThrows(IllegalArgumentException.class, () ->
                accountServiceImpl.getMoney(accId, BigDecimal.valueOf(2))
        );
        Assertions.assertEquals("Not enough money", e.getMessage());
    }

    @Test
    void getAccount() {
        Mockito.when(accountDaoMock.getAccount(testAccId)).thenReturn(testAcc);

        Account res = accountServiceImpl.getAccount(testAccId);
        Assertions.assertEquals(testAcc, res);
    }

    @Test
    void checkBalance() {
        Mockito.when(accountDaoMock.getAccount(testAccId)).thenReturn(testAcc);
        BigDecimal res = accountServiceImpl.checkBalance(testAccId);
        Assertions.assertEquals(BigDecimal.valueOf(84), res);
    }
}
