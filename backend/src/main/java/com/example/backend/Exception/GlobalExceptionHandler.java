// package com.example.backend.Exception;

// import java.time.LocalDateTime;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.ControllerAdvice;
// import org.springframework.web.bind.annotation.ExceptionHandler;
// import org.springframework.web.context.request.WebRequest;

// import com.example.backend.DTO.Response.ErrorResponse;

// @ControllerAdvice 
// public class GlobalExceptionHandler {

    
//     @ExceptionHandler(ResourceNotFoundException.class)
//     public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
//         ErrorResponse errorResponse = new ErrorResponse(
//                 HttpStatus.NOT_FOUND.value(),
//                 ex.getMessage(),
//                 LocalDateTime.now()
//         );
//         return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
//     }

   
//     @ExceptionHandler(IllegalArgumentException.class)
//     public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
//         ErrorResponse errorResponse = new ErrorResponse(
//                 HttpStatus.BAD_REQUEST.value(),
//                 ex.getMessage(),
//                 LocalDateTime.now()
//         );
//         return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
//     }

   
//     @ExceptionHandler(Exception.class)
//     public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
//         ErrorResponse errorResponse = new ErrorResponse(
//                 HttpStatus.INTERNAL_SERVER_ERROR.value(),
//                 "An unexpected error occurred: " + ex.getMessage(),
//                 LocalDateTime.now()
//         );
//         return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
//     }
// }