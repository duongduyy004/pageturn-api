package com.pageturn.backend.collection;

import com.pageturn.backend.collection.dto.AddBookToCollectionRequest;
import com.pageturn.backend.collection.dto.CollectionDto;
import com.pageturn.backend.collection.dto.CreateCollectionRequest;
import com.pageturn.backend.collection.dto.UpdateCollectionRequest;
import com.pageturn.backend.common.exception.NotFoundException;
import com.pageturn.backend.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final CollectionBookRepository collectionBookRepository;
    private final UserService userService;

    public CollectionService(CollectionRepository collectionRepository,
                             CollectionBookRepository collectionBookRepository,
                             UserService userService) {
        this.collectionRepository = collectionRepository;
        this.collectionBookRepository = collectionBookRepository;
        this.userService = userService;
    }

    @Transactional
    public CollectionDto create(Long userId, CreateCollectionRequest request) {
        Collection collection = new Collection();
        collection.setUser(userService.getEntityById(userId));
        collection.setName(request.name().trim());
        collection.setDescription(request.description());
        return toDto(collectionRepository.save(collection));
    }

    @Transactional
    public CollectionDto update(Long userId, Long collectionId, UpdateCollectionRequest request) {
        Collection collection = getCollection(userId, collectionId);
        collection.setName(request.name().trim());
        collection.setDescription(request.description());
        return toDto(collectionRepository.save(collection));
    }

    @Transactional(readOnly = true)
    public List<CollectionDto> list(Long userId) {
        return collectionRepository.findAllByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CollectionDto> listUpdatedAfter(Long userId, Instant updatedAfter) {
        return collectionRepository.findAllByUserIdAndUpdatedAtAfter(userId, updatedAfter).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public CollectionDto addBook(Long userId, Long collectionId, AddBookToCollectionRequest request) {
        Collection collection = getCollection(userId, collectionId);

        collectionBookRepository.findByCollectionIdAndBookHash(collectionId, request.bookHash()).ifPresentOrElse(
                existing -> moveBook(existing, request.position()),
                () -> insertBook(collection, request.bookHash(), request.position())
        );

        collection.touch();
        collectionRepository.save(collection);
        return toDto(collection);
    }

    @Transactional
    public CollectionDto removeBook(Long userId, Long collectionId, String bookHash) {
        Collection collection = getCollection(userId, collectionId);
        collectionBookRepository.findByCollectionIdAndBookHash(collectionId, bookHash).ifPresent(book -> {
            int removedPosition = book.getPosition();
            collectionBookRepository.delete(book);
            shiftPositionsLeft(collectionId, removedPosition);
        });
        collection.touch();
        collectionRepository.save(collection);
        return toDto(collection);
    }

    @Transactional
    public CollectionDto replaceBooks(Long userId, Long collectionId, List<CollectionDto.CollectionBookItem> books) {
        Collection collection = getCollection(userId, collectionId);
        List<CollectionBook> existingBooks = collectionBookRepository.findAllByCollectionIdOrderByPositionAscCreatedAtAsc(collectionId);
        collectionBookRepository.deleteAll(existingBooks);

        if (books != null) {
            for (CollectionDto.CollectionBookItem item : books) {
                insertBook(collection, item.bookHash(), item.position());
            }
        }

        collection.touch();
        collectionRepository.save(collection);
        return toDto(collection);
    }

    @Transactional
    public void delete(Long userId, Long collectionId) {
        collectionRepository.delete(getCollection(userId, collectionId));
    }

    @Transactional(readOnly = true)
    public Collection getCollection(Long userId, Long collectionId) {
        return collectionRepository.findByIdAndUserId(collectionId, userId)
                .orElseThrow(() -> new NotFoundException("Collection not found"));
    }

    private void insertBook(Collection collection, String bookHash, Integer requestedPosition) {
        int position = normalizePosition(collection.getId(), requestedPosition);
        shiftPositionsRight(collection.getId(), position);

        CollectionBook collectionBook = new CollectionBook();
        collectionBook.setCollection(collection);
        collectionBook.setBookHash(bookHash);
        collectionBook.setPosition(position);
        collectionBookRepository.save(collectionBook);
    }

    private void moveBook(CollectionBook existing, Integer requestedPosition) {
        int currentPosition = existing.getPosition();
        int targetPosition = normalizePosition(existing.getCollection().getId(), requestedPosition);
        if (currentPosition == targetPosition) {
            return;
        }

        List<CollectionBook> books = collectionBookRepository.findAllByCollectionIdOrderByPositionAscCreatedAtAsc(existing.getCollection().getId());
        books.removeIf(book -> book.getId().equals(existing.getId()));
        if (targetPosition > books.size()) {
            targetPosition = books.size();
        }
        books.add(targetPosition, existing);
        reindexBooks(books);
    }

    private void shiftPositionsRight(Long collectionId, int fromPosition) {
        List<CollectionBook> books = collectionBookRepository.findAllByCollectionIdOrderByPositionAscCreatedAtAsc(collectionId);
        for (CollectionBook book : books) {
            if (book.getPosition() >= fromPosition) {
                book.setPosition(book.getPosition() + 1);
            }
        }
        collectionBookRepository.saveAll(books);
    }

    private void shiftPositionsLeft(Long collectionId, int removedPosition) {
        List<CollectionBook> books = collectionBookRepository.findAllByCollectionIdOrderByPositionAscCreatedAtAsc(collectionId);
        for (CollectionBook book : books) {
            if (book.getPosition() > removedPosition) {
                book.setPosition(book.getPosition() - 1);
            }
        }
        collectionBookRepository.saveAll(books);
    }

    private void reindexBooks(List<CollectionBook> books) {
        for (int i = 0; i < books.size(); i++) {
            books.get(i).setPosition(i);
        }
        collectionBookRepository.saveAll(books);
    }

    private int normalizePosition(Long collectionId, Integer requestedPosition) {
        int size = collectionBookRepository.findAllByCollectionIdOrderByPositionAscCreatedAtAsc(collectionId).size();
        if (requestedPosition == null || requestedPosition < 0 || requestedPosition > size) {
            return size;
        }
        return requestedPosition;
    }

    private CollectionDto toDto(Collection collection) {
        List<CollectionDto.CollectionBookItem> books = collectionBookRepository
                .findAllByCollectionIdOrderByPositionAscCreatedAtAsc(collection.getId())
                .stream()
                .map(book -> new CollectionDto.CollectionBookItem(book.getBookHash(), book.getPosition()))
                .toList();
        return new CollectionDto(
                collection.getId(),
                collection.getName(),
                collection.getDescription(),
                books,
                collection.getCreatedAt(),
                collection.getUpdatedAt()
        );
    }
}
