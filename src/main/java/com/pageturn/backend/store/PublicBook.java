package com.pageturn.backend.store;

import com.pageturn.backend.common.entity.AuditableEntity;
import com.pageturn.backend.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "public_books")
public class PublicBook extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_hash", nullable = false, unique = true, length = 64)
    private String bookHash;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 255)
    private String author;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 32)
    private String language;

    @Column(name = "cover_key", length = 1024)
    private String coverKey;

    @Column(name = "file_key", nullable = false, length = 1024)
    private String fileKey;

    @Column(name = "file_format", nullable = false, length = 16)
    private String fileFormat;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(length = 120)
    private String category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "added_by", nullable = false)
    private User addedBy;

    @Column(name = "download_count", nullable = false)
    private long downloadCount;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public Long getId() {
        return id;
    }

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

    public String getCoverKey() {
        return coverKey;
    }

    public void setCoverKey(String coverKey) {
        this.coverKey = coverKey;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public User getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(User addedBy) {
        this.addedBy = addedBy;
    }

    public long getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(long downloadCount) {
        this.downloadCount = downloadCount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
