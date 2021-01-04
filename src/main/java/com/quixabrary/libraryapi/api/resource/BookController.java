package com.quixabrary.libraryapi.api.resource;

import com.quixabrary.libraryapi.api.dto.BookDTO;
import com.quixabrary.libraryapi.api.exception.ApiErrors;
import com.quixabrary.libraryapi.api.model.entity.Book;
import com.quixabrary.libraryapi.exception.BusinessException;
import com.quixabrary.libraryapi.service.BookService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService service;
    private ModelMapper modelMapper;

    public BookController(BookService service, ModelMapper modelMapper) {
        this.service = service;
        this.modelMapper = modelMapper;
    }

    //padrao dto: é uma classe simples com atributos simples com fim de representar os dados da requisição e converter em objeto json
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    //corpo que é mandado na requisição sera convertido no dto
    public BookDTO create(@RequestBody @Valid BookDTO dto){ // notation valid vai vaidar o objeto baseado nas suas notations
        //cria instancia de Book e transfere todas propriedades de mesmo nome para o objeto Book
        Book entity = modelMapper.map(dto, Book.class);
        entity = service.save(entity);

        //pega a entidade e transforma em um BookDTO
        return modelMapper.map(entity, BookDTO.class);
    }

    @GetMapping("{id}")
    public BookDTO get(@PathVariable long id){
        return service
                .getById(id)
                .map(book -> modelMapper.map(book, BookDTO.class))
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND));

    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id){
        Book book = service
                .getById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND));
        service.delete(book);
    }

    @PutMapping("{id}")
    public BookDTO update( @PathVariable long id, BookDTO dto){
        return service
                .getById(id).map(book -> {
                    book.setAuthor(dto.getAuthor());
                    book.setTitle(dto.getTitle());
                    book = service.update(book);
                    return modelMapper.map(book, BookDTO.class);
                }).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    //quando tenta validar objeto mas ele nao esta valido
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleValidationExeptions(MethodArgumentNotValidException ex){
        BindingResult bindingResult = ex.getBindingResult();
        return  new ApiErrors(bindingResult);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleBusinessException(BusinessException ex){
        return new ApiErrors(ex);
    }
}
