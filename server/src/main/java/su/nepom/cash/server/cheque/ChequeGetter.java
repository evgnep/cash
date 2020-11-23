package su.nepom.cash.server.cheque;

import su.nepom.cash.dto.ChequeDto;

public interface ChequeGetter {
    ChequeDto getCheque(String qr);
}
