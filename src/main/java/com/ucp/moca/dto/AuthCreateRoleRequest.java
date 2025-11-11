package com.ucp.moca.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public record AuthCreateRoleRequest(
        @Size(max = 2, message = "El usuario no puede tener más de dos roles") List<String> roleListName) {
}