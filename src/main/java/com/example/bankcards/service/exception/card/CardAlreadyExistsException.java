package com.example.bankcards.service.exception.card;

public class CardAlreadyExistsException extends RuntimeException {
  public CardAlreadyExistsException(String message) {
    super(message);
  }
}
