package com.ucp.moca.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"email","message","jwt","status"})
public record AuthResponse(Integer id ,
                           String email,
                           String message,
                           String jwt,
                           boolean status) {
}
