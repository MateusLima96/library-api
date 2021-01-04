package com.quixabrary.libraryapi.service;

import com.quixabrary.libraryapi.api.model.entity.Book;

import java.util.Optional;

public interface BookService {
    Book save(Book save);

    Optional<Book> getById(Long id);

    void delete(Book book);

    Book update(Book book);
}
