package com.neo.customerservice.dto;

import com.neo.customerservice.dto.user.input.LoginUserInputDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LoginUserInputDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private LoginUserInputDto buildValidDto() {
        return LoginUserInputDto.builder()
                .username("testuser")
                .password("password123")
                .build();
    }

    @Test
    void validInput_noErrors() {
        LoginUserInputDto dto = buildValidDto();

        Set<ConstraintViolation<LoginUserInputDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void blankUsername_validationError() {
        LoginUserInputDto dto = LoginUserInputDto.builder()
                .username("")
                .password("password123")
                .build();

        Set<ConstraintViolation<LoginUserInputDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    void blankPassword_validationError() {
        LoginUserInputDto dto = LoginUserInputDto.builder()
                .username("testuser")
                .password("")
                .build();

        Set<ConstraintViolation<LoginUserInputDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }
}
