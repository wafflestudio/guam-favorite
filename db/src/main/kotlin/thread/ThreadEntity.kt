package waffle.guam.thread

import waffle.guam.comment.CommentView
import waffle.guam.image.ImageEntity
import waffle.guam.user.UserEntity
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table

@Table(name = "threads")
@Entity
data class ThreadEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    val projectId: Long,

    @Column(name = "user_id")
    val userId: Long,

    val content: String?,

    val createdAt: Instant = Instant.now(),

    val modifiedAt: Instant = createdAt,
)

@Table(name = "threads")
@Entity
data class ThreadView(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    val projectId: Long,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: UserEntity,

    @OneToMany(mappedBy = "threadId", fetch = FetchType.LAZY)
    val comments: List<CommentView>,

    @OneToMany(mappedBy = "parentId", fetch = FetchType.LAZY)
    val images: List<ImageEntity>,

    val content: String?,

    val createdAt: Instant = Instant.now(),

    val modifiedAt: Instant = createdAt,
)
