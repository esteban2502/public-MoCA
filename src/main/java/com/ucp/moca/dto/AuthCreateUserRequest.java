package com.ucp.moca.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

@Validated
public record AuthCreateUserRequest(

        @NotBlank String name,
        @NotBlank String idNumber,
        @NotBlank String email,
        @NotBlank String password,
        @Valid AuthCreateRoleRequest roleRequest
) {
}