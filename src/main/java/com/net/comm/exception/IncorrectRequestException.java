package com.net.comm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception which will be thrown when incoming request
 * doesn't pass the validation.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IncorrectRequestException extends RuntimeException {

    /**
	 * 
	 */

	public IncorrectRequestException(String message) {
        super(message);
    }
}
