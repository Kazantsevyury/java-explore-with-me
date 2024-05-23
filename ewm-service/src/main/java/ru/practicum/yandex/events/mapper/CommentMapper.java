package ru.practicum.yandex.events.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.yandex.events.dto.AddCommentDto;
import ru.practicum.yandex.events.dto.CommentDto;
import ru.practicum.yandex.events.dto.ShortCommentDto;
import ru.practicum.yandex.events.dto.UpdateCommentDto;
import ru.practicum.yandex.events.model.Comment;
import ru.practicum.yandex.user.mapper.UserMapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface CommentMapper {

    CommentDto toDto(Comment comment);

    Comment toModel(AddCommentDto addCommentDto);

    @Mapping(source = "commentId", target = "id")
    Comment toModel(UpdateCommentDto updateCommentDto);

    ShortCommentDto toShortDto(Comment comment);

    List<AddCommentDto> toDtoList(List<Comment> comments);
}
