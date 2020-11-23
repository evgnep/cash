package su.nepom.cash.server.cheque;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import su.nepom.cash.dto.ChequeDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cheque")
public class ChequeController {
    private final ChequeGetter chequeGetter;

    @GetMapping("/{qrCode}")
    ChequeDto getCheque(@PathVariable String qrCode) {
        return chequeGetter.getCheque(qrCode);
    }
}
