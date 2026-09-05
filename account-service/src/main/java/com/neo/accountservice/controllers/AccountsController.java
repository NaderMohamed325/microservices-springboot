package com.neo.accountservice.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountsController {


    @GetMapping()
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<String> getAccounts() {
        return ResponseEntity.ok("Accounts endpoint is working!");
    }


}
