package com.pageturn.backend.store;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PublicBookRepository extends JpaRepository<PublicBook, Long> {

    boolean existsByBookHash(String bookHash);

    Optional<PublicBook> findByIdAndActiveTrue(Long id);

    @Query("""
            select b from PublicBook b
            where b.active = true
              and (:category is null or lower(b.category) = lower(:category))
              and (
                    :keyword is null
                    or lower(b.title) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(b.author, '')) like lower(concat('%', :keyword, '%'))
              )
            """)
    Page<PublicBook> searchActive(
            @Param("category") String category,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
