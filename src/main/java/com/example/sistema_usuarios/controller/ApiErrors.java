package com.example.sistema_usuarios.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiErrors {
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ProblemDetail conflict(DataIntegrityViolationException error) {
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.CONFLICT, "Registro duplicado ou operação incompatível com os dados.");
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail validation(MethodArgumentNotValidException error) {
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST,
        "Revise os campos: "
            + error.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField())
                .distinct()
                .toList());
  }
}
