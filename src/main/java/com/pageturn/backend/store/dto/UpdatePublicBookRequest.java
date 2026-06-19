package com.pageturn.backend.store.dto;

import jakarta.validation.constraints.Pattern;
import org.springframework.web.multipart.MultipartFile;

public class UpdatePublicBookRequest {

    @Pattern(regexp = "^[a-f0-9]{64}$")
    private String bookHash;

    private String title;
    private String author;
    private String description;
    private String language;
    private String category;
    private Boolean active;
    private MultipartFile file;
    private MultipartFile coverImage;

    public String getBookHash() {
        return bookHash;
    }

    public void setBookHash(String bookHash) {
        this.bookHash = bookHash;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public MultipartFile getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(MultipartFile coverImage) {
        this.coverImage = coverImage;
    }
}
