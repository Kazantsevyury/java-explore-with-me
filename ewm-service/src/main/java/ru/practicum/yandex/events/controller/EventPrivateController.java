package ru.practicum.yandex.events.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.yandex.events.dto.AddCommentDto;
import ru.practicum.yandex.events.dto.EventFullDto;
import ru.practicum.yandex.events.dto.UpdateCommentDto;
import ru.practicum.yandex.events.mapper.CommentMapper;
import ru.practicum.yandex.events.mapper.EventMapper;
import ru.practicum.yandex.events.model.Comment;
import ru.practicum.yandex.events.model.Event;
import ru.practicum.yandex.events.service.EventService;

import javax.validation.Valid;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class EventPrivateController {

    private final EventService eventService;

    private final EventMapper eventMapper;

    private final CommentMapper commentMapper;

    /**
     * Add comment to event. If comment added successfully, returns 201 response status.
     *
     * @param userId        user adding comment
     * @param eventId       event to comment
     * @param addCommentDto comment parameters
     * @return added comment
     */
    @PostMapping("/{eventId}/comment/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addCommentToEvent(@PathVariable Long userId,
                                          @PathVariable Long eventId,
                                          @RequestBody @Valid AddCommentDto addCommentDto) {
        log.info("User with id '{}' adding comment to event with id '{}'.", userId, eventId);
        final Comment comment = commentMapper.toModel(addCommentDto);
        final Event commentedEvent = eventService.addCommentToEvent(userId, eventId, comment);
        return eventMapper.toDto(commentedEvent);
    }

    /**
     * Update comment.
     *
     * @param userId           user updating comment
     * @param eventId          event comment to update
     * @param updateCommentDto update comment
     * @return updated comment
     */
    @PatchMapping("/{eventId}/comment/{userId}")
    public EventFullDto updateComment(@PathVariable Long userId,
                                      @PathVariable Long eventId,
                                      @RequestBody @Valid UpdateCommentDto updateCommentDto) {
        log.info("User with id '{}' updating comment with id '{}'.", userId, updateCommentDto.getCommentId());
        final Comment commentRequest = commentMapper.toModel(updateCommentDto);
        final Event commentedEvent = eventService.updateComment(userId, eventId, commentRequest);
        return eventMapper.toDto(commentedEvent);
    }

    /**
     * Delete comment.
     *
     * @param userId    user deleting comment
     * @param commentId comment id to delete
     */
    @DeleteMapping("/{userId}/comment/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId,
                              @PathVariable Long commentId) {
        log.info("User with id '{}' updating comment with id '{}'.", userId, commentId);
        eventService.deleteComment(userId, commentId);
    }
}
