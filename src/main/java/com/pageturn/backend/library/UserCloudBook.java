package com.pageturn.backend.library;

import com.pageturn.backend.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "user_cloud_books")
public class UserCloudBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "book_hash", nullable = false, length = 64)
    private String bookHash;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 255)
    private String author;

    @Column(name = "file_format", length = 16)
    private String fileFormat;

    @Column(name = "cover_key", length = 1024)
    private String coverKey;

    @Column(name = "file_key", length = 1024)
    private String fileKey;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "cloud_synced", nullable = false)
    private boolean cloudSynced;

    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (addedAt == null) {
            addedAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
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

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public boolean isCloudSynced() {
        return cloudSynced;
    }

    public void setCloudSynced(boolean cloudSynced) {
        this.cloudSynced = cloudSynced;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Instant addedAt) {
        this.addedAt = addedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
