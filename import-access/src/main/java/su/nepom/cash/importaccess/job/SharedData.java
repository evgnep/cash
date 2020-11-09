package su.nepom.cash.importaccess.job;

import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.stereotype.Component;
import su.nepom.cash.importaccess.domain.Account;
import su.nepom.cash.importaccess.domain.Currency;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Данные, разделяемые между разными шагами
 */
@Component
@JobScope
public class SharedData {
    private final Map<Long, Currency> currencies = new HashMap<>();
    private final Map<Long, Account> accounts = new HashMap<>();

    public Currency getCurrency(long id) {
        return currencies.get(id);
    }

    public void addCurrency(Currency currency) {
        currencies.put(currency.getId(), currency);
    }

    public Account getAccount(long id) {
        return accounts.get(id);
    }

    public void addAccount(Account account) {
        accounts.put(account.getId(), account);
    }

    public Collection<Account> getAccounts() {
        return accounts.values();
    }
}
