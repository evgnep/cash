package su.nepom.cash.phone.utils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AppError {
    private final String message;

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Ошибка: " + message;
    }
}
