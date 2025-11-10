package com.e_sim.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import java.io.Serializable;
import java.time.Instant;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiRes<T> implements Serializable {

    private String responseCode;
    private String responseMessage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    @JsonIgnore
    private HttpStatus httpStatus;

    private String timestamp;

    public static <T> ApiRes<T> success(T data, HttpStatus httpStatus) {
        return new ApiRes<>("200", "success", data, httpStatus, Instant.now().toString());
    }

    public static ApiRes success(HttpStatus httpStatus) {
        return new ApiRes<>("200", "success", null, httpStatus, Instant.now().toString());
    }

    public static <T> ApiRes<T> error(T data, HttpStatus httpStatus) {
        return new ApiRes<>("500", "error", data, httpStatus, Instant.now().toString());
    }

    public static <T> ApiRes<T> error(T data) {
        return new ApiRes<>("500", "error", data, null, Instant.now().toString());
    }
}