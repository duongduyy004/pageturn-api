package com.pageturn.backend.progress;

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
@Table(name = "reading_progress")
public class ReadingProgress extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "book_hash", nullable = false, length = 64)
    private String bookHash;

    @Column(name = "chapter_idx", nullable = false)
    private int chapterIdx;

    @Column(name = "scroll_pct", nullable = false)
    private double scrollPct;

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

    public int getChapterIdx() {
        return chapterIdx;
    }

    public void setChapterIdx(int chapterIdx) {
        this.chapterIdx = chapterIdx;
    }

    public double getScrollPct() {
        return scrollPct;
    }

    public void setScrollPct(double scrollPct) {
        this.scrollPct = scrollPct;
    }
}
