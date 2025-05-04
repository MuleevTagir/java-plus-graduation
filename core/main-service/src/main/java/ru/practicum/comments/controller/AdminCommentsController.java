package ru.practicum.comments.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.dto.CommentRequestDto;
import ru.practicum.comments.dto.CommentResponseDto;
import ru.practicum.comments.service.CommentService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/{eventId}/comments")
@Slf4j
public class AdminCommentsController {
    private final CommentService commentService;

    @PatchMapping("/{commentId}")
    public CommentResponseDto updateComment(@PathVariable Long eventId,
                                            @PathVariable Long commentId,
                                            @RequestBody CommentRequestDto commentRequestDto) {
        log.info("Updating comment with id={} and body={}", commentId, commentRequestDto);
        CommentResponseDto response = commentService.updateCommentAsAdmin(eventId, commentId, commentRequestDto);
        log.info("Send response with body = {}", response);
        return response;
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long eventId, @PathVariable Long commentId) {
        log.info("Deleting comment with id={}", commentId);
        commentService.deleteCommentAsAdmin(eventId, commentId);
        log.info("Comment with id = {} deleted", commentId);
    }
}
