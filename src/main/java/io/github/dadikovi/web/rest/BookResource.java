package io.github.dadikovi.web.rest;

import io.github.dadikovi.config.ShelfChangedSender;
import io.github.dadikovi.domain.Book;
import io.github.dadikovi.repository.BookRepository;
import io.github.dadikovi.web.rest.errors.BadRequestAlertException;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link io.github.dadikovi.domain.Book}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class BookResource {

    private final Logger log = LoggerFactory.getLogger(BookResource.class);

    private static final String ENTITY_NAME = "libraryShelfBook";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final BookRepository bookRepository;

    private final ShelfChangedSender shelfChangedSender;

    public BookResource( BookRepository bookRepository, ShelfChangedSender shelfChangedSender ) {
        this.bookRepository = bookRepository;
        this.shelfChangedSender = shelfChangedSender;
    }

    /**
     * {@code POST  /books} : Create a new book.
     *
     * @param book the book to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new book, or with status {@code 400 (Bad Request)} if the book has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/books")
    @ApiOperation("Create a new book.")
    @Transactional
    public ResponseEntity<Book> createBook(@ApiParam(
        name = "book",
        type = "Book",
        value = "The book to be created."
    ) @RequestBody Book book) throws URISyntaxException {
        log.debug("REST request to save Book : {}", book);
        if (book.getId() != null) {
            throw new BadRequestAlertException("A new book cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Book result = bookRepository.save(book);
        shelfChangedSender.created(book);
        return ResponseEntity.created(new URI("/api/books/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /books} : Updates an existing book.
     *
     * @param book the book to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated book,
     * or with status {@code 400 (Bad Request)} if the book is not valid,
     * or with status {@code 500 (Internal Server Error)} if the book couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/books")
    @ApiOperation("Updates an existing book.")
    @Transactional
    public ResponseEntity<Book> updateBook(@ApiParam(
        name = "book",
        type = "Book",
        value = "The id of this book will identify to book which should be updated. It will be updated to match the given attributes of this parameter."
    ) @RequestBody Book book) throws URISyntaxException {
        log.debug("REST request to update Book : {}", book);
        if (book.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        Book result = bookRepository.save(book);
        shelfChangedSender.updated(book);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, book.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /books-filtered} : get all the books filtered by the provided attribute values.
     *
     * @param book the example which will be the param of the query by example query
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the filtered list of books in body.
     */
    @GetMapping("/books-filtered")
    @ApiOperation("Gets all books which are matching with the provided example.")
    public List<Book> getAllBooksByExample(@ApiParam(
        name = "book",
        type = "Book",
        value = "The example which will be the param of the query-by-example query. "
        + "A book will be returned if and only if all of the field values equal with the field values of this parameter."
    ) @Valid Book book) {
        log.debug("REST request to get filtered Book : {}", book);
        return bookRepository.findAll(Example.of(book));
    }

    /**
     * {@code GET  /books} : get all the books.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of books in body.
     */
    @GetMapping("/books")
    @ApiOperation("Gets all books.")
    public List<Book> getAllBooks() {
        log.debug("REST request to get all Books");
        return bookRepository.findAll();
    }

    /**
     * {@code GET  /books/:id} : get the "id" book.
     *
     * @param id the id of the book to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the book, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/books/{id}")
    @ApiOperation("Gets a given book by its id.")
    public ResponseEntity<Book> getBook(@ApiParam(
        name = "id",
        type = "Long",
        value = "The ID of the required book."
    ) @PathVariable Long id) {
        log.debug("REST request to get Book : {}", id);
        Optional<Book> book = bookRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(book);
    }

    /**
     * {@code DELETE  /books/:id} : delete the "id" book.
     *
     * @param id the id of the book to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/books/{id}")
    @ApiOperation("Deletes a given book by its id.")
    @Transactional
    public ResponseEntity<Void> deleteBook(@ApiParam(
        name = "id",
        type = "Long",
        value = "The ID of the book to delete."
    ) @PathVariable Long id) {
        log.debug("REST request to delete Book : {}", id);
        bookRepository.deleteById(id);
        Book deleted = new Book();
        deleted.setId(id);
        shelfChangedSender.deleted(deleted);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
    }
}
