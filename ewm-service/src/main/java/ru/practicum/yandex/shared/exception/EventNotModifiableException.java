package ru.practicum.yandex.shared.exception;

public class EventNotModifiableException extends RuntimeException {
    public EventNotModifiableException(String message) {
        super(message);
    }
}
