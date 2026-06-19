package com.pageturn.backend.collection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollectionBookRepository extends JpaRepository<CollectionBook, Long> {

    List<CollectionBook> findAllByCollectionIdOrderByPositionAscCreatedAtAsc(Long collectionId);

    Optional<CollectionBook> findByCollectionIdAndBookHash(Long collectionId, String bookHash);
}
