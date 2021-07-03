package waffle.guam.db.entity

import java.time.LocalDateTime
import javax.persistence.*

@Table(name = "comments")
@Entity
data class CommentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    val threadId: Long,

    @Column(name = "user_id")
    val userId: Long,

    val content: String?,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    val modifiedAt: LocalDateTime = createdAt,
)

@Table(name = "comments")
@Entity
data class CommentView(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    val threadId: Long,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: UserEntity,

    @OneToMany(mappedBy = "parentId", fetch = FetchType.EAGER)
    val images: List<ImageEntity>,

    val content: String,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    val modifiedAt: LocalDateTime = createdAt,
)
