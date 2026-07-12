// dto/LoginResponse.java
package com.transitops.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String token;
    private String email;
    private String name;
    private String role;
}