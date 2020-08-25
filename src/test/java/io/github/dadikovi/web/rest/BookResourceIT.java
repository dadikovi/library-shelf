package io.github.dadikovi.web.rest;

import io.github.dadikovi.LibraryShelfApp;
import io.github.dadikovi.config.ShelfChangedSender;
import io.github.dadikovi.domain.Book;
import io.github.dadikovi.domain.ShelfChangedMessage;
import io.github.dadikovi.domain.enumeration.ChangeType;
import io.github.dadikovi.repository.BookRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link BookResource} REST controller.
 */
@SpringBootTest(classes = LibraryShelfApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class BookResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_AUTHOR = "AAAAAAAAAA";
    private static final String UPDATED_AUTHOR = "BBBBBBBBBB";

    private static final String DEFAULT_PUBLISHER = "AAAAAAAAAA";
    private static final String UPDATED_PUBLISHER = "BBBBBBBBBB";

    private static final Long DEFAULT_PUBLISH_YEAR = 1L;
    private static final Long UPDATED_PUBLISH_YEAR = 2L;

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Long DEFAULT_COUNT = 1L;
    private static final Long UPDATED_COUNT = 2L;
    public static final String WAR_AND_PEACE = "War and Peace";
    public static final String LEO_TOLSTOY = "Leo Tolstoy";

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restBookMockMvc;

    @MockBean(name = "template")
    private AmqpTemplate template;

    private Book book;

    private static Book hitchhikersGuideToTheGalaxy() {
        return new Book()
            .title("The Hitchhiker's Guide to the Galaxy")
            .author("Douglas Adams")
            .publisher("Megadodo Publications")
            .publishYear(1978L)
            .createdAt(Instant.now())
            .count(2L);
    }

    private static Book warAndPeace() {
        return new Book()
            .title(WAR_AND_PEACE)
            .author(LEO_TOLSTOY)
            .publisher("The Russian Messenger")
            .publishYear(1869L)
            .createdAt(Instant.now())
            .count(2L);
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Book createEntity(EntityManager em) {
        Book book = new Book()
            .title(DEFAULT_TITLE)
            .author(DEFAULT_AUTHOR)
            .publisher(DEFAULT_PUBLISHER)
            .publishYear(DEFAULT_PUBLISH_YEAR)
            .createdAt(DEFAULT_CREATED_AT)
            .count(DEFAULT_COUNT);
        return book;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Book createUpdatedEntity(EntityManager em) {
        Book book = new Book()
            .title(UPDATED_TITLE)
            .author(UPDATED_AUTHOR)
            .publisher(UPDATED_PUBLISHER)
            .publishYear(UPDATED_PUBLISH_YEAR)
            .createdAt(UPDATED_CREATED_AT)
            .count(UPDATED_COUNT);
        return book;
    }

    @BeforeEach
    public void initTest() {
        book = createEntity(em);
    }

    @Test
    @Transactional
    public void createBook() throws Exception {
        int databaseSizeBeforeCreate = bookRepository.findAll().size();
        // Create the Book
        restBookMockMvc.perform(post("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(book)))
            .andExpect(status().isCreated());

        // Validate the Book in the database
        List<Book> bookList = bookRepository.findAll();
        assertThat(bookList).hasSize(databaseSizeBeforeCreate + 1);
        Book testBook = bookList.get(bookList.size() - 1);
        assertThat(testBook.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testBook.getAuthor()).isEqualTo(DEFAULT_AUTHOR);
        assertThat(testBook.getPublisher()).isEqualTo(DEFAULT_PUBLISHER);
        assertThat(testBook.getPublishYear()).isEqualTo(DEFAULT_PUBLISH_YEAR);
        assertThat(testBook.getCreatedAt()).isEqualTo(DEFAULT_CREATED_AT);
        assertThat(testBook.getCount()).isEqualTo(DEFAULT_COUNT);

        Mockito.verify(template).convertAndSend(eq("shelfChanged"), eq(new ShelfChangedMessage(ChangeType.CREATE, testBook)));
    }

    @Test
    @Transactional
    public void createBookWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = bookRepository.findAll().size();

        // Create the Book with an existing ID
        book.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restBookMockMvc.perform(post("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(book)))
            .andExpect(status().isBadRequest());

        // Validate the Book in the database
        List<Book> bookList = bookRepository.findAll();
        assertThat(bookList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllBooks() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get all the bookList
        restBookMockMvc.perform(get("/api/books?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(book.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].author").value(hasItem(DEFAULT_AUTHOR)))
            .andExpect(jsonPath("$.[*].publisher").value(hasItem(DEFAULT_PUBLISHER)))
            .andExpect(jsonPath("$.[*].publishYear").value(hasItem(DEFAULT_PUBLISH_YEAR.intValue())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].count").value(hasItem(DEFAULT_COUNT.intValue())));
    }

    @Test
    @Transactional
    public void getOneBookByExample() throws Exception {
        // Insert two books
        bookRepository.saveAndFlush(warAndPeace());
        bookRepository.saveAndFlush(hitchhikersGuideToTheGalaxy());

        // Check if two books are returned
        restBookMockMvc.perform(get("/api/books-filtered?title=" + WAR_AND_PEACE + "&author=" + LEO_TOLSTOY))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].title").value(hasItem(WAR_AND_PEACE)))
            .andExpect(jsonPath("$.[*].author").value(hasItem(LEO_TOLSTOY)))
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @Transactional
    public void getAllBooksByExample() throws Exception {
        // Insert two books
        bookRepository.saveAndFlush(warAndPeace());
        bookRepository.saveAndFlush(hitchhikersGuideToTheGalaxy());

        // Check if two books are returned
        restBookMockMvc.perform(get("/api/books-filtered?count=2"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @Transactional
    public void getAllBooksByNonExistingExample() throws Exception {
        // Insert two books
        bookRepository.saveAndFlush(warAndPeace());
        bookRepository.saveAndFlush(hitchhikersGuideToTheGalaxy());

        // Check if no books are returned
        restBookMockMvc.perform(get("/api/books-filtered?title=NON_EXISTING_TITLE&author=" + DEFAULT_AUTHOR))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @Transactional
    public void getBook() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        // Get the book
        restBookMockMvc.perform(get("/api/books/{id}", book.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(book.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.author").value(DEFAULT_AUTHOR))
            .andExpect(jsonPath("$.publisher").value(DEFAULT_PUBLISHER))
            .andExpect(jsonPath("$.publishYear").value(DEFAULT_PUBLISH_YEAR.intValue()))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.count").value(DEFAULT_COUNT.intValue()));
    }
    @Test
    @Transactional
    public void getNonExistingBook() throws Exception {
        // Get the book
        restBookMockMvc.perform(get("/api/books/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateBook() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        int databaseSizeBeforeUpdate = bookRepository.findAll().size();

        // Update the book
        Book updatedBook = bookRepository.findById(book.getId()).get();
        // Disconnect from session so that the updates on updatedBook are not directly saved in db
        em.detach(updatedBook);
        updatedBook
            .title(UPDATED_TITLE)
            .author(UPDATED_AUTHOR)
            .publisher(UPDATED_PUBLISHER)
            .publishYear(UPDATED_PUBLISH_YEAR)
            .createdAt(UPDATED_CREATED_AT)
            .count(UPDATED_COUNT);

        restBookMockMvc.perform(put("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedBook)))
            .andExpect(status().isOk());

        // Validate the Book in the database
        List<Book> bookList = bookRepository.findAll();
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate);
        Book testBook = bookList.get(bookList.size() - 1);
        assertThat(testBook.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testBook.getAuthor()).isEqualTo(UPDATED_AUTHOR);
        assertThat(testBook.getPublisher()).isEqualTo(UPDATED_PUBLISHER);
        assertThat(testBook.getPublishYear()).isEqualTo(UPDATED_PUBLISH_YEAR);
        assertThat(testBook.getCreatedAt()).isEqualTo(UPDATED_CREATED_AT);
        assertThat(testBook.getCount()).isEqualTo(UPDATED_COUNT);

        Mockito.verify(template).convertAndSend(eq("shelfChanged"), eq(new ShelfChangedMessage(ChangeType.UPDATE, testBook)));
    }

    @Test
    @Transactional
    public void updateNonExistingBook() throws Exception {
        int databaseSizeBeforeUpdate = bookRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBookMockMvc.perform(put("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(book)))
            .andExpect(status().isBadRequest());

        // Validate the Book in the database
        List<Book> bookList = bookRepository.findAll();
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteBook() throws Exception {
        // Initialize the database
        bookRepository.saveAndFlush(book);

        int databaseSizeBeforeDelete = bookRepository.findAll().size();

        // Delete the book
        restBookMockMvc.perform(delete("/api/books/{id}", book.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Book> bookList = bookRepository.findAll();
        assertThat(bookList).hasSize(databaseSizeBeforeDelete - 1);

        Book deletedBook = new Book();
        deletedBook.setId(book.getId());

        Mockito.verify(template).convertAndSend(eq("shelfChanged"), eq(new ShelfChangedMessage(ChangeType.DELETE, deletedBook)));
    }
}
