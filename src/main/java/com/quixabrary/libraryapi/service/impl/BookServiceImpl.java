package com.quixabrary.libraryapi.service.impl;

import com.quixabrary.libraryapi.api.model.entity.Book;
import com.quixabrary.libraryapi.exception.BusinessException;
import com.quixabrary.libraryapi.model.repository.BookRepository;
import com.quixabrary.libraryapi.service.BookService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {
    private BookRepository repository;

    public BookServiceImpl(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public Book save(Book book) {
        if(repository.existsByIsbn(book.getIsbn())){ //query method ja faz a verificacao se isbn existe na base de dados
            throw new BusinessException("isbn já cadastrado");
        }
        return repository.save(book);
    }

    @Override
    public Book update(Book book) {
        return null;
    }

    @Override
    public void delete(Book book) {

    }

    @Override
    public Optional<Book> getById(Long id) {
        return this.repository.findById(id);
    }
}
