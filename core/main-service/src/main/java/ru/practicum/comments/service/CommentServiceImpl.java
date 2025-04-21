package ru.practicum.comments.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.comments.dto.CommentRequestDto;
import ru.practicum.comments.dto.CommentResponseDto;
import ru.practicum.comments.mapper.CommentMapper;
import ru.practicum.comments.model.Comment;
import ru.practicum.comments.repository.CommentRepository;
import ru.practicum.errors.exceptions.DataConflictException;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public CommentResponseDto createComment(Long eventId, Long userId, CommentRequestDto commentRequestDto) {

        User user = userRepository.getUserById(userId);
        log.info("User = {}", user.toString());


        Comment comment = commentMapper.toComment(commentRequestDto, user, eventId, LocalDateTime.now());
        log.info("Comment = {}", comment);

        log.info("Comment for creation = {}", comment);

        return commentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentResponseDto> getCommentByTargetId(Long id) {
        List<Comment> comments = commentRepository.findAllByEventId(id);
        return commentMapper.toDtoList(comments);
    }

    @Override
    public CommentResponseDto getCommentByCommentId(Long eventId, Long id) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("No event with this id = " + eventId));
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No comment with id = " + id));
        return commentMapper.toDto(comment);
    }

    @Override
    public List<CommentResponseDto> getUserComments(Long id) {
        User user = userRepository.getUserById(id);
        List<Comment> comments = commentRepository.findAllByAuthor(user);
        return commentMapper.toDtoList(comments);
    }

    @Override
    public CommentResponseDto updateCommentAsAuthor(Long eventId, Long commentId, Long userId,
                                                    CommentRequestDto commentDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Not found event with id = " + eventId));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("No comment with id =" + commentId));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new DataConflictException("Not user cannot update comment");
        }
        comment.setText(commentDto.getText());
        return commentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    public CommentResponseDto updateCommentAsAdmin(Long eventId, Long commentId, CommentRequestDto commentDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Not found event with id = " + eventId));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("No comment with id =" + commentId));
        comment.setText(commentDto.getText());
        return commentMapper.toDto(commentRepository.save(comment));

    }

    @Override
    public void deleteCommentAsAuthor(Long eventId, Long userId, Long commentId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Not found event with id = " + eventId));
        log.info("Event = {}", event);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("No comment with id =" + commentId));
        log.info("Comment = {}", comment);
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new DataConflictException("Not user cannot delete comment");
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public void deleteCommentAsAdmin(Long eventId, Long commentId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Not found event with id = " + eventId));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("No comment with id =" + commentId));
        commentRepository.deleteById(commentId);
    }
}
