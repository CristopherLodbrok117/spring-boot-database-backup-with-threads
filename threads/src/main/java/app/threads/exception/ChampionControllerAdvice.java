package app.threads.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ChampionControllerAdvice {

    @ExceptionHandler(ChampionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String championNotFoundHandler(ChampionNotFoundException ex){
        return ex.getMessage();
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String championConflictHandler(DataAccessException ex){
        return ex.getMessage();
    }

}
