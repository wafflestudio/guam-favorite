package waffle.guam.comment

import waffle.guam.comment.command.CreateComment
import waffle.guam.comment.command.DeleteComment
import waffle.guam.comment.command.DeleteCommentImage
import waffle.guam.comment.command.EditCommentContent
import waffle.guam.comment.event.CommentContentEdited
import waffle.guam.comment.event.CommentCreated
import waffle.guam.comment.event.CommentDeleted
import waffle.guam.comment.event.CommentImageDeleted
import waffle.guam.comment.model.Comment

interface CommentService {
    fun getComment(commentId: Long): Comment
    fun createComment(command: CreateComment): CommentCreated
    fun editCommentContent(command: EditCommentContent): CommentContentEdited
    fun deleteCommentImage(command: DeleteCommentImage): CommentImageDeleted
    fun deleteComment(command: DeleteComment): CommentDeleted
}
