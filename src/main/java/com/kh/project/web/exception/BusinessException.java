package com.kh.project.web.exception;

import lombok.Getter;

/**
 * 비즈니스 예외
 */
@Getter
public class BusinessException extends RuntimeException {
  public BusinessException(String message) {
    super(message);
  }

  public BusinessException(String message, Throwable cause) {
    super(message, cause);
  }
}