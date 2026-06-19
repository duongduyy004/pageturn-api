package com.pageturn.backend.highlight;

import com.pageturn.backend.common.exception.NotFoundException;
import com.pageturn.backend.highlight.dto.CreateHighlightRequest;
import com.pageturn.backend.highlight.dto.HighlightDto;
import com.pageturn.backend.highlight.dto.UpdateHighlightRequest;
import com.pageturn.backend.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class HighlightService {

    private final HighlightRepository highlightRepository;
    private final UserService userService;

    public HighlightService(HighlightRepository highlightRepository, UserService userService) {
        this.highlightRepository = highlightRepository;
        this.userService = userService;
    }

    @Transactional
    public HighlightDto create(Long userId, CreateHighlightRequest request) {
        validateOffsets(request.startOffset(), request.endOffset());

        Highlight highlight = new Highlight();
        highlight.setUser(userService.getEntityById(userId));
        highlight.setBookHash(request.bookHash());
        highlight.setChapterIdx(request.chapterIdx());
        highlight.setStartOffset(request.startOffset());
        highlight.setEndOffset(request.endOffset());
        highlight.setTextContent(request.textContent());
        highlight.setColor(request.color());
        highlight.setNote(request.note());
        highlight.setDeleted(false);
        return toDto(highlightRepository.save(highlight));
    }

    @Transactional(readOnly = true)
    public List<HighlightDto> listByBook(Long userId, String bookHash) {
        return highlightRepository.findAllByUserIdAndBookHashAndIsDeletedFalseOrderByCreatedAtDesc(userId, bookHash).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HighlightDto> listUpdatedAfter(Long userId, Instant updatedAfter) {
        return highlightRepository.findAllByUserIdAndUpdatedAtAfterOrderByUpdatedAtAsc(userId, updatedAfter).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public HighlightDto update(Long userId, Long highlightId, UpdateHighlightRequest request) {
        validateOffsets(request.startOffset(), request.endOffset());

        Highlight highlight = getEntity(userId, highlightId);
        highlight.setChapterIdx(request.chapterIdx());
        highlight.setStartOffset(request.startOffset());
        highlight.setEndOffset(request.endOffset());
        highlight.setTextContent(request.textContent());
        highlight.setColor(request.color());
        highlight.setNote(request.note());
        return toDto(highlightRepository.save(highlight));
    }

    @Transactional
    public void delete(Long userId, Long highlightId) {
        Highlight highlight = getEntity(userId, highlightId);
        highlight.setDeleted(true);
        highlightRepository.save(highlight);
    }

    @Transactional(readOnly = true)
    public Highlight getEntity(Long userId, Long highlightId) {
        return highlightRepository.findByIdAndUserId(highlightId, userId)
                .orElseThrow(() -> new NotFoundException("Highlight not found"));
    }

    private HighlightDto toDto(Highlight highlight) {
        return new HighlightDto(
                highlight.getId(),
                highlight.getBookHash(),
                highlight.getChapterIdx(),
                highlight.getStartOffset(),
                highlight.getEndOffset(),
                highlight.getTextContent(),
                highlight.getColor(),
                highlight.getNote(),
                highlight.getCreatedAt(),
                highlight.getUpdatedAt(),
                highlight.isDeleted()
        );
    }

    private void validateOffsets(int startOffset, int endOffset) {
        if (startOffset > endOffset) {
            throw new IllegalArgumentException("startOffset must be less than or equal to endOffset");
        }
    }
}
