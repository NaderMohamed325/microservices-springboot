package com.neo.customerservice.dto;

import com.neo.customerservice.dto.user.input.UpdateUserInputDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateUserInputDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private UpdateUserInputDto buildValidDto() {
        return UpdateUserInputDto.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .password("password123")
                .build();
    }

    @Test
    void validInput_noErrors() {
        UpdateUserInputDto dto = buildValidDto();

        Set<ConstraintViolation<UpdateUserInputDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void nullId_throwsNullPointerException() {
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () ->
                UpdateUserInputDto.builder()
                        .id(null)
                        .email("test@example.com")
                        .username("testuser")
                        .password("password123")
                        .build()
        );
    }

    @Test
    void blankUsername_validationError() {
        UpdateUserInputDto dto = UpdateUserInputDto.builder()
                .id(1L)
                .email("test@example.com")
                .username("")
                .password("password123")
                .build();

        Set<ConstraintViolation<UpdateUserInputDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    void passwordTooShort_validationError() {
        UpdateUserInputDto dto = UpdateUserInputDto.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .password("short")
                .build();

        Set<ConstraintViolation<UpdateUserInputDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    void passwordTooLong_validationError() {
        UpdateUserInputDto dto = UpdateUserInputDto.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .password("a".repeat(31))
                .build();

        Set<ConstraintViolation<UpdateUserInputDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    void invalidEmail_validationError() {
        UpdateUserInputDto dto = UpdateUserInputDto.builder()
                .id(1L)
                .email("not-an-email")
                .username("testuser")
                .password("password123")
                .build();

        Set<ConstraintViolation<UpdateUserInputDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }
}
