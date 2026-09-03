package com.neo.customerservice.services.auth;

import com.neo.customerservice.dto.user.input.CreateUserInputDto;
import com.neo.customerservice.dto.user.input.LoginUserInputDto;
import com.neo.customerservice.dto.user.output.LoginUserOutputDto;
import com.neo.customerservice.dto.user.output.UserOutputDto;

public interface AuthService {

    LoginUserOutputDto login(LoginUserInputDto loginUserInputDto);

    UserOutputDto registerUser(CreateUserInputDto createUserInputDto);

}
