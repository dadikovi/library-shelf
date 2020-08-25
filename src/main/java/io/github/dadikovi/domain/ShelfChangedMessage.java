package io.github.dadikovi.domain;

import io.github.dadikovi.domain.enumeration.ChangeType;

public class ShelfChangedMessage {

    private ChangeType changeType;
    private Book changedBook;

    @Override
    public boolean equals( Object o ) {
        if ( this == o )
            return true;
        if ( o == null || getClass() != o.getClass() )
            return false;

        ShelfChangedMessage that = (ShelfChangedMessage) o;

        if ( changeType != that.changeType )
            return false;
        return changedBook != null ? changedBook.equals(that.changedBook) : that.changedBook == null;
    }

    @Override
    public int hashCode() {
        int result = changeType != null ? changeType.hashCode() : 0;
        result = 31 * result + (changedBook != null ? changedBook.hashCode() : 0);
        return result;
    }

    public ShelfChangedMessage( ChangeType changeType, Book changedBook ) {
        this.changeType = changeType;
        this.changedBook = changedBook;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType( ChangeType changeType ) {
        this.changeType = changeType;
    }

    public Book getChangedBook() {
        return changedBook;
    }

    public void setChangedBook( Book changedBook ) {
        this.changedBook = changedBook;
    }
}
