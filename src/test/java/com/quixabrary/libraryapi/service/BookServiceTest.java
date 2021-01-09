package com.quixabrary.libraryapi.service;

import com.quixabrary.libraryapi.api.model.entity.Book;
import com.quixabrary.libraryapi.exception.BusinessException;
import com.quixabrary.libraryapi.model.repository.BookRepository;
import com.quixabrary.libraryapi.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(SpringExtension.class) //subir contexto do Spring com apenas o que é necessário
@ActiveProfiles("test")
public class BookServiceTest {
    BookService service;

    @MockBean //implementacao para repository
    BookRepository repository;

    @BeforeEach
    public void setUp(){
        this.service = new BookServiceImpl(repository);
    }

    private Book createValidBook() {
        return Book.builder().isbn("123").author("fulano").title("titulo").build();
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest(){
        //cenario
        Book book = createValidBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(false);
        Mockito.when(repository.save(book)) //so retorna se passar a instancia do livro
                    .thenReturn(
                            Book.builder()
                                    .id(1l)
                                    .isbn("123")
                                    .title("titulo")
                                    .author("fulano")
                                    .build());

        //execucao
        Book savedBook = service.save(book);

        //verificacao
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("123");
        assertThat(savedBook.getTitle()).isEqualTo("titulo");
        assertThat(savedBook.getAuthor()).isEqualTo("fulano");
    }

    @Test
    @DisplayName("Deve lançar erro de negocio ao tentar salvar um livro com isbn duplicado")
    public void shouldNotSaveABookWithDuplicatedISBN(){
        //cenario
        Book book = createValidBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        //execucao
        Throwable exception = Assertions.catchThrowable(() -> service.save(book));

        //verificacoes
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("isbn já cadastrado");
        Mockito.verify(repository, Mockito.never()).save(book); // verifica que repository nunca vai executar metodo save com o parametro passado
    }

    @Test
    @DisplayName("Deve obter livro por Id")
    public void getByIdTest(){
        Long id = 1l;

        Book book = createValidBook();
        book.setId(id);
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));

        //execucao
        Optional<Book> foundBook = service.getById(id);

        //verificacoes
        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getId()).isEqualTo(id);
        assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
    }

    @Test
    @DisplayName("Deve retornar vazio ao obter um livro por Id quando ele nao existe na base")
    public void bookNotFoundByIdTest(){
        Long id = 1l;

        Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

        //execucao
        Optional<Book> book = service.getById(id);

        //verificacoes
        assertThat(book.isPresent()).isFalse();

    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBookTest(){
        //cenario
        Book updatingBook = Book.builder().id(1l).build();

        //simulacao
        Book updatedBook = createValidBook();
        updatedBook.setId(1l);


        Mockito.when(repository.save(updatingBook)).thenReturn(updatedBook);

        //execucao
        Book book = service.update(updatingBook);

        //verifica
        assertThat(book.getId()).isEqualTo(updatedBook.getId());
        assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
        assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
        assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());
    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar atualizar um livro inexistente")
    public void updateInvalidBookTest(){
        Book book = new Book();

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.update(book)); // verifica que lance a exeception esperada quando executar metodo delete

        Mockito.verify( repository, Mockito.never()).save(book);//verifica que nunca chamou metodo delete passando argumento
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest(){
        //cenario
        Book book = Book.builder().id(1l).build();


        //execucao
        assertDoesNotThrow(() -> service.delete(book)); //verifica que executavel nao lance nenhum erro

        //verificacao
     Mockito.verify(repository, Mockito.times(1)).delete(book);// veridico que que chamei o metodo delete uma vez passando objeto
    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar deletar livro inexistente")
    public void deleteInvalidBookTest(){
        Book book = new Book();

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.delete(book)); // verifica que lance a exeception esperada quando executar metodo delete

        Mockito.verify( repository, Mockito.never()).delete(book);//verifica que nunca chamou metodo delete passando argumento
    }

    @Test
    @DisplayName("Deve filtrar livros pelas propriedades")
    public void findBookTest(){
            Book book = createValidBook();

            PageRequest pageRequest = PageRequest.of(0, 10);

            List<Book> lista = Arrays.asList(book);
            Page<Book> page = new PageImpl<Book>(lista, pageRequest, 1);
            Mockito.when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class))).thenReturn(page);

            Page<Book> result = service.find(book, pageRequest);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).isEqualTo(lista);
            assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
            assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

}
