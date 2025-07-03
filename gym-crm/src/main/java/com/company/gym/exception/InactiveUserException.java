package com.company.gym.exception;

public class InactiveUserException extends RuntimeException {
  public InactiveUserException(String message) {
    super(message);
  }
}
