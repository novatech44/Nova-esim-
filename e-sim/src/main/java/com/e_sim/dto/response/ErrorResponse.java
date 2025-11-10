package com.e_sim.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard error response format")
public record ErrorResponse(
        @Schema(example = "500", description = "Error code indicating the type of error")
        String responseCode,

        @Schema(example = "error", description = "Constant string indicating error state")
        String responseMessage,

        @Schema(example = "Detailed error message", description = "Additional error details or message")
        Object data
) {
}