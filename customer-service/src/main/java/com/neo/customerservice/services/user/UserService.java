package com.neo.customerservice.services.user;

import com.neo.customerservice.dto.user.input.CreateUserInputDto;
import com.neo.customerservice.dto.user.input.UpdateUserInputDto;
import com.neo.customerservice.dto.user.output.UserOutputDto;
import com.neo.customerservice.entity.User;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;

public interface UserService {
    UserOutputDto createUser(@NonNull CreateUserInputDto createUserDto);

    UserOutputDto getUserByUsername(@NonNull String username);

    Page<UserOutputDto> getCustomersPaginated(int page, int size, String sortBy, String sortDir);


    UserOutputDto updateUser(Long userId, UpdateUserInputDto updateUserInputDto);


    User getUserById(Long userId);

    UserOutputDto getUserByIdOutputDto(Long userId);

    void deleteUser(Long userId);
}