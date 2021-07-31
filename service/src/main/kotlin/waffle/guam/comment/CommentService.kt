package waffle.guam.comment

import waffle.guam.comment.command.CreateComment
import waffle.guam.comment.command.DeleteComment
import waffle.guam.comment.command.DeleteCommentImage
import waffle.guam.comment.command.EditCommentContent
import waffle.guam.comment.event.CommentCreated
import waffle.guam.comment.event.CommentDeleted

interface CommentService {
    fun createComment(command: CreateComment): CommentCreated
    fun editCommentContent(command: EditCommentContent): Boolean
    fun deleteCommentImage(command: DeleteCommentImage): Boolean
    fun deleteComment(command: DeleteComment): CommentDeleted
}