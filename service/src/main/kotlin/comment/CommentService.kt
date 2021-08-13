package waffle.guam.comment

import waffle.guam.comment.command.CreateComment
import waffle.guam.comment.command.DeleteComment
import waffle.guam.comment.command.EditCommentContent
import waffle.guam.comment.event.CommentContentEdited
import waffle.guam.comment.event.CommentCreated
import waffle.guam.comment.event.CommentDeleted

interface CommentService {
    fun createComment(command: CreateComment): CommentCreated
    fun editCommentContent(command: EditCommentContent): CommentContentEdited
    fun deleteComment(command: DeleteComment): CommentDeleted
}
