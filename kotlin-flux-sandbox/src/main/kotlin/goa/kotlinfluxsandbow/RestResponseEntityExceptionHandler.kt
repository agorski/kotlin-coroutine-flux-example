package goa.kotlinfluxsandbow

import goa.kotlinfluxsandbow.controllers.CustomException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class RestResponseEntityExceptionHandler {
    @ExceptionHandler(CustomException::class)
    fun handleAccessDeniedException(ex: CustomException): ResponseEntity<String> {
        return ResponseEntity.ok("ex.message")
    }
}