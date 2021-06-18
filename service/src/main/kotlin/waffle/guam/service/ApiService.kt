package waffle.guam.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Service
import waffle.guam.db.User.UserDTO
import waffle.guam.db.User.UserReadDTO
import java.io.IOException

@Service
class ApiService(
    private val client: OkHttpClient = OkHttpClient()
) {

    private val objectMapper = ObjectMapper().registerKotlinModule()

    fun fetchUser(idList: List<Long>): List<UserReadDTO>{

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val url = "http://34.84.37.132/user/list?userIds="
        val res: MutableList<UserReadDTO> = ArrayList()

        var query: String = ""
        idList.map {
            query += "$it,"
        }
        val responseBody = fetch( url + query.subSequence(0, query.length-1) )
        val data = objectMapper.readValue( responseBody, Map::class.java )["data"]

        val json = objectMapper.writeValueAsString(data)
        val dto = objectMapper.readValue(json , Array<UserReadDTO>::class.java )
        res.addAll(dto)

        return res
    }

    @Throws(IOException::class)
    fun fetch(path: String): String {
        val request = Request.Builder().url(path).build()
        client.newCall(request).execute().use {
                response -> return response.body()!!.string()
        }
    }
}