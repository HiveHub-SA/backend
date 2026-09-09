// VerifyCodeRequest.java
package com.hivehub.app.login.dto.resetPassword;

import jakarta.validation.constraints.NotBlank;

public record VerifyCodeRequest(
        @NotBlank String email,
        @NotBlank String code
) {}