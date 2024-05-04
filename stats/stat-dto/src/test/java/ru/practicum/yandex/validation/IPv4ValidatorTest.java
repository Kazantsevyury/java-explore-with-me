package ru.practicum.yandex.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.*;

public class IPv4ValidatorTest {

    private IPv4Validator ipv4Validator;

    @Mock
    private ValidIPv4 validIPv4;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ipv4Validator = new IPv4Validator();
    }

    @Test
    public void testInitializeWhenCalledThenNoException() {
        assertDoesNotThrow(() -> ipv4Validator.initialize(validIPv4));
    }

    @Test
    public void testInitializeWhenCalledWithValidAnnotationThenNoException() {
        assertDoesNotThrow(() -> ipv4Validator.initialize(validIPv4));
    }

    @Test
    public void testIsValidWhenCalledWithValidIPv4ThenReturnTrue() {
        String validIPv4 = "192.168.1.1";
        boolean result = ipv4Validator.isValid(validIPv4, constraintValidatorContext);
        assertTrue(result, "Expected true when valid IPv4 is passed");
    }

}