package ru.zakharova.alyona.dto;

import java.sql.Date;

public class JournalRecord {
    private final int id;
    private String lastName;
    private String firstName;
    private String bookName;
    private final Date dateBegin;
    private Date dateEnd;
    private Date dateReturn;

    public JournalRecord(int id, String lastName, String firstName, String bookName, Date dateBegin, Date dateEnd, Date dateReturn) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.bookName = bookName;
        this.dateBegin = dateBegin;
        this.dateEnd = dateEnd;
        this.dateReturn = dateReturn;
    }

    public int getId() {
        return id;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getBookName() {
        return bookName;
    }

    public Date getDateBegin() {
        return dateBegin;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public Date getDateReturn() {
        return dateReturn;
    }

    @Override
    public String toString() {
        return "JournalRecord{" +
                "id=" + id +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", bookName='" + bookName + '\'' +
                ", dateBegin=" + dateBegin +
                ", dateEnd=" + dateEnd +
                ", dateReturn=" + dateReturn +
                '}';
    }
}
