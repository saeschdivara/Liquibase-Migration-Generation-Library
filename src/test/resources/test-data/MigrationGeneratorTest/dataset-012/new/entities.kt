package foo

import jakarta.persistence.*


@Entity
class Permission(
    @Column(nullable = false)
    var name: String,

    @ManyToMany
    @JoinTable(
        name = "group_permission",
        joinColumns = [JoinColumn(name = "permission_id")],
        inverseJoinColumns = [JoinColumn(name = "group_id")]
    )
    var groups: Set<Group>,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,
)

@Entity
class Group(
    @Column(nullable = false)
    var name: String,

    @ManyToMany(mappedBy = "groups")
    var permissions: Set<Permission>,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,
)