package ru.practicum.yandex.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;

@RestControllerAdvice
public class StatServiceExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(StatServiceExceptionHandler.class);

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.error("Type Mismatch: ", e);
        return "Неверный тип для параметра '" + e.getName() + "': требуемый тип " + e.getRequiredType().getSimpleName() + ", получен " + e.getValue();
    }

    @ExceptionHandler(InvalidDateRangeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleDateRangeException(InvalidDateRangeException e) {
        log.error("Date Range Error: ", e);
        return "Ошибка диапазона дат: " + e.getMessage();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleArgumentNotValid(MethodArgumentNotValidException e) {
        log.error("Argument Not Valid: ", e);
        return "Ошибка валидации параметра: " + e.getBindingResult().getFieldError().getField() + " - " + e.getBindingResult().getFieldError().getDefaultMessage();
    }

    @ExceptionHandler(DateTimeParseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleDateTimeParseException(DateTimeParseException e) {
        log.error("DateTime Parsing Error: ", e);
        return "Ошибка формата даты: " + e.getParsedString() + ", ожидается формат 'yyyy-MM-dd HH:mm:ss'";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneralException(Exception e) {
        log.error("Internal Server Error: ", e);
        return "Произошла внутренняя ошибка сервера. Пожалуйста, обратитесь в службу поддержки.";
    }

}
