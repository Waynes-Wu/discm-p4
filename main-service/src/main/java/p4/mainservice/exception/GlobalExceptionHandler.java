package p4.mainservice.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public ModelAndView handleServiceException(ServiceException ex) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("status", ex.getStatus().value());
        mav.addObject("error", ex.getStatus().getReasonPhrase());
        mav.addObject("message", ex.getMessage());
        return mav;
    }

    @ExceptionHandler(AuthenticationException.class)
    public String handleAuthenticationException(AuthenticationException ex) {
        return "redirect:/login?error=" + ex.getMessage();
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDeniedException(AccessDeniedException ex) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("status", HttpStatus.FORBIDDEN.value());
        mav.addObject("error", "Access Denied");
        mav.addObject("message", "You do not have permission to access this resource");
        return mav;
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ModelAndView handleWebClientResponseException(WebClientResponseException ex) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("status", ex.getStatusCode().value());
        mav.addObject("error", "Service Error");
        mav.addObject("message", "Error communicating with service: " + ex.getMessage());
        return mav;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public String handleValidationException(ConstraintViolationException ex) {
        return "redirect:/error?message=" + ex.getMessage();
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception ex) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        mav.addObject("error", "Internal Server Error");
        mav.addObject("message", ex.getMessage());
        return mav;
    }
}
