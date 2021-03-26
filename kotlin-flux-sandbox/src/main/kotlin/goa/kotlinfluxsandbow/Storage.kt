package goa.kotlinfluxsandbow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitOneOrNull
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*


@Component
class Storage(
    val dbClient: DatabaseClient,
    val template: R2dbcEntityTemplate
) {
    suspend fun getAll(): Flow<Texts> {
        return template.select(Texts::class.java).all().asFlow()
    }

    suspend fun clearDB() {
        dbClient.sql("delete from texts").fetch().awaitOneOrNull()
    }

    suspend fun fillWithTest(howMany: Int) {
        (1..howMany).forEach { _: Int ->
            template
                .insert(Texts::class.java)
                .using(Texts(someText = UUID.randomUUID().toString()))
                .awaitFirst()
        }
    }

    suspend fun insertWithCoroutine(text: String): Int? {
        return template
            .insert(Texts::class.java)
            .using(Texts(someText = text.take(100)))
            .awaitSingle()
            .id
    }

    fun insertWithMono(text: String): Mono<Int?> {
        return mono {
            insertWithCoroutine(text)
        }
    }

}

data class Texts(@Id val id: Int? = null, val someText: String)

