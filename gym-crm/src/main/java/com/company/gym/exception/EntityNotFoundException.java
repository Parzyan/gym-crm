package com.company.gym.exception;

public class EntityNotFoundException extends ServiceException {
  public EntityNotFoundException(String message) {
    super(message);
  }

  public EntityNotFoundException(String entityName, String identifier) {
    super(String.format("%s not found with identifier: %s", entityName, identifier));
  }
}
