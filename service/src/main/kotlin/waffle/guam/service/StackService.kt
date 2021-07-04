package waffle.guam.service

import org.springframework.stereotype.Service
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.TechStackEntity
import waffle.guam.db.repository.StackRepository
import waffle.guam.model.TechStack
import javax.persistence.EntityNotFoundException

@Service
class StackService(
    private val stackRepository: StackRepository
) {

    private val searchEngine: SearchEngine = SearchEngine()

    fun init() {
        val stream = this.javaClass.getResourceAsStream("/stacks.csv")
        val reader = java.io.InputStreamReader(stream)
        reader.forEachLine {
            val idx = it.indexOf(",")
            val p = Position.values().random()
            stackRepository.save(
                TechStackEntity(
                    name = it.dropLast(it.length - (idx)),
                    aliases = (it.drop(idx + 2)).dropLast(1),
                    position = p
                )
            )
        }
    }

    fun create(o: TechStack): Boolean {
        stackRepository.save(o.toEntity())
        return true
    }

    fun getAll(): List<TechStack> {
        val target = stackRepository.findAll()
        return target.map { TechStack.of(it) }
    }

    /*
     # 여러가지 키워드로 검색했을 경우: 검색어마다 OR 처리 -> AND 위주로 보여줌
     # case Insensitive
    */
    fun searchByKeyword(query: String): List<TechStack> {
        val map = mutableMapOf<TechStack, Int>()

        val devTypes = getAll()
        for (dev in devTypes) {
            val mappings = dev.aliases.split(", ")
            val cnt = searchEngine.search(mappings, query)
            if (cnt > 0) map[dev] = cnt
        }
        return map.toList().sortedWith(compareBy { -it.second }).map { it.first }
    }

    // fixme : throw an error? or create one?
    fun searchIdByDTO(o: TechStack): Long {
        val target = stackRepository.findAll().filter { TechStack.of(it) == o }
        return if (target.isNotEmpty()) target[0].id else {
            // make new entity
            // val res = o.toEntity()
            // stackRepository.save(res)
            // res.id
            throw EntityNotFoundException()
        }
    }

    fun searchById(id: Long): TechStackEntity {
        val res = stackRepository.findById(id)
        return res.get()
    }
}
