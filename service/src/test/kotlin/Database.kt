package waffle.guam

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import javax.persistence.Table

@Component
class Database(
    private val entityManager: EntityManager
) {

    private val tableNames: List<String> =
        entityManager.metamodel.entities.mapNotNull { it.javaType.getAnnotation(Table::class.java)?.name }

    @Transactional
    fun cleanUp() {
        entityManager.flush()
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate()

        for (tableName in tableNames.toSet()) {
            entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate()
            entityManager.createNativeQuery("ALTER TABLE " + tableName + " ALTER COLUMN ID RESTART WITH 1")
                .executeUpdate()
        }

        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate()
    }
}
