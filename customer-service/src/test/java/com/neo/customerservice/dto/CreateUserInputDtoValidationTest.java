package com.neo.customerservice.dto;

import com.neo.customerservice.dto.user.input.CreateUserInputDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateUserInputDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private CreateUserInputDto buildValidDto() {
        return CreateUserInputDto.builder()
                .email("test@example.com")
                .username("testuser")
                .password("password123")
                .build();
    }

    @Test
    void validInput_noErrors() {
        CreateUserInputDto dto = buildValidDto();

        Set<ConstraintViolation<CreateUserInputDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void blankUsername_validationError() {
        CreateUserInputDto dto = CreateUserInputDto.builder()
                .email("test@example.com")
                .username("")
                .password("password123")
                .build();

        Set<ConstraintViolation<CreateUserInputDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    void blankPassword_validationError() {
        CreateUserInputDto dto = CreateUserInputDto.builder()
                .email("test@example.com")
                .username("testuser")
                .password("")
                .build();

        Set<ConstraintViolation<CreateUserInputDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    void invalidEmail_validationError() {
        CreateUserInputDto dto = CreateUserInputDto.builder()
                .email("not-an-email")
                .username("testuser")
                .password("password123")
                .build();

        Set<ConstraintViolation<CreateUserInputDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void passwordTooShort_validationError() {
        CreateUserInputDto dto = CreateUserInputDto.builder()
                .email("test@example.com")
                .username("testuser")
                .password("short")
                .build();

        Set<ConstraintViolation<CreateUserInputDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    void passwordTooLong_validationError() {
        CreateUserInputDto dto = CreateUserInputDto.builder()
                .email("test@example.com")
                .username("testuser")
                .password("a".repeat(31))
                .build();

        Set<ConstraintViolation<CreateUserInputDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }
}
