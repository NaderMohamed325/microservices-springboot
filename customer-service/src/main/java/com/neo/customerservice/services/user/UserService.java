package com.neo.customerservice.services.user;
import com.neo.customerservice.dto.user.input.CreateUserInputDto;
import com.neo.customerservice.dto.user.output.UserOutputDto;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;

public interface UserService {
    UserOutputDto createUser(@NonNull CreateUserInputDto createUserDto);

    UserOutputDto getUserByUsername(@NonNull String username);

    Page<UserOutputDto> getCustomersPaginated(int page, int size, String sortBy, String sortDir);
}
