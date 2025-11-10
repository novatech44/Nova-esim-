package com.e_sim.exception;

import lombok.experimental.StandardException;
import org.springframework.security.core.AuthenticationException;

@StandardException
public class UnauthorizedException extends AuthenticationException {
}