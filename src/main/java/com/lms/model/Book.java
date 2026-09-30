package com.lms.model;

/** Book model mapped to books table. */
public class Book {
    private int id;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private String category;
    private int totalCopies;
    private int availableCopies;
    private String shelf;
    private double price;
    private String createdAt;

    public Book() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getTotalCopies() { return totalCopies; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }

    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }

    public String getShelf() { return shelf; }
    public void setShelf(String shelf) { this.shelf = shelf; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
