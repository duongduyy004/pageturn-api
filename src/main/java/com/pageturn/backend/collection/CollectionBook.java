package com.pageturn.backend.collection;

import com.pageturn.backend.common.entity.AuditableEntity;
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
@Table(name = "collection_books")
public class CollectionBook extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;

    @Column(name = "book_hash", nullable = false, length = 64)
    private String bookHash;

    @Column(name = "position", nullable = false)
    private int position;

    public Long getId() {
        return id;
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    public String getBookHash() {
        return bookHash;
    }

    public void setBookHash(String bookHash) {
        this.bookHash = bookHash;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
