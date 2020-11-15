package com.quixabrary.libraryapi.api.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;

@Data
@Builder //gera um builder da classe com as propriedades dela; facilita criação das instâncias
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
    private long id;

    @NotEmpty
    private String title;

    @NotEmpty
    private String author;

    @NotEmpty
    private String isbn;

}
