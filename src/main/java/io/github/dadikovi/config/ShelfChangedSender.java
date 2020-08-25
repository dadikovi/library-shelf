package io.github.dadikovi.config;

import io.github.dadikovi.domain.Book;
import io.github.dadikovi.domain.ShelfChangedMessage;
import io.github.dadikovi.domain.enumeration.ChangeType;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class ShelfChangedSender {

    @Autowired
    private AmqpTemplate template;

    @Autowired
    private Queue queue;

    public void created(Book book) {
        this.template.convertAndSend(queue.getName(), new ShelfChangedMessage(ChangeType.CREATE, book));
    }

    public void updated(Book book) {
        this.template.convertAndSend(queue.getName(), new ShelfChangedMessage(ChangeType.UPDATE, book));
    }

    public void deleted(Book book) {
        this.template.convertAndSend(queue.getName(), new ShelfChangedMessage(ChangeType.DELETE, book));
    }
}
