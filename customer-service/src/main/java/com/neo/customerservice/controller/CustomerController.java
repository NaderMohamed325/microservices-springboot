package com.neo.customerservice.controller;

import com.neo.customerservice.dto.user.output.UserOutputDto;
import com.neo.customerservice.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final UserService userService;


    @GetMapping()
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    public Page<UserOutputDto> getAllCustomers(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      @RequestParam(defaultValue = "id") String sortBy,
                                      @RequestParam(defaultValue = "asc") String sortDir) {
        return userService.getCustomersPaginated(page, size, sortBy, sortDir);
    }

}
