package goa.kotlinfluxsandbow.controllers

import goa.kotlinfluxsandbow.Storage
import goa.kotlinfluxsandbow.Texts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.util.*

@Suppress("unused")
@RestController
class WebCallAndStorageApi(val storage: Storage) {

    private val webClient: WebClient = WebClient.create()

    @GetMapping("/db/all")
    suspend fun storageTest(): Flow<Texts> {
        return storage.getAll()
    }

    // in real world should be post
    @GetMapping("/db/clear")
    suspend fun clear() {
        return storage.clearDB()
    }

    // in real world should be post
    @GetMapping("/fill-with-test/{howMany}")
    suspend fun fillWithTestData(@PathVariable howMany: Int) {
        val howManySafe = if (howMany > 0) howMany else 1
        print(UUID.randomUUID().toString())
        return storage.fillWithTest(howManySafe)
    }

    @GetMapping("/blocking/{operation}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun withBlocking(@PathVariable operation: String): String? {
        return prepareWebCall(operation)
            .exchangeToMono { r ->
                if (r.statusCode().is2xxSuccessful) {
                    r.bodyToMono(String::class.java)
                } else {
                    Mono.error(CoinNotFoundError("$operation not found"))
                }
            }
            // ugly, blocking overhead; you can also use blocking web client
            .subscribeOn(Schedulers.parallel())
            .share()
            .block(Duration.ofSeconds(5L))
    }

    @GetMapping("/flux/{operation}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun withFlux(@PathVariable operation: String): Mono<String> {
        return prepareWebCall(operation)
            .exchangeToMono { r ->
                if (r.statusCode().is2xxSuccessful) {
                    r.bodyToMono(String::class.java)
                } else {
                    Mono.error(CoinNotFoundError("$operation not found"))
                }
            }
    }

    @GetMapping("/flux/{operation}/store", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun withFluxStore(@PathVariable operation: String): Mono<String> {
        return prepareWebCall(operation)
            .exchangeToMono { clientResponse ->
                if (clientResponse.statusCode().is2xxSuccessful) {
                    clientResponse
                        .bodyToMono(String::class.java)
                        .flatMap { text -> storage.insertWithMono(text) }
                        .map { id -> "inserted id $id" }
                } else {
                    Mono.error(CoinNotFoundError("$operation not found"))
                }
            }
    }

    @GetMapping("/coroutine/{operation}", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun withCoroutine(@PathVariable operation: String): ResponseEntity<String> {
        return prepareWebCall(operation)
            .awaitExchange { clientResponse ->
                if (clientResponse.statusCode().is2xxSuccessful) {
                    ResponseEntity.ok(clientResponse.awaitBody())
                } else {
                    ResponseEntity.notFound().build()
                }
            }
    }

    @GetMapping("/coroutine/{operation}/store", produces = [MediaType.TEXT_PLAIN_VALUE])
    suspend fun withCoroutineStore(@PathVariable operation: String): ResponseEntity<String> {
        return prepareWebCall(operation)
            .awaitExchange { clientResponse ->
                if (clientResponse.statusCode().is2xxSuccessful) {
                    val text = clientResponse.awaitBody<String>()
                    val id: Int? = storage.insertWithCoroutine(text)
                    ResponseEntity.status(200).body("inserted id $id")
                } else {
                    ResponseEntity.notFound().build()
                }
            }
    }

    @GetMapping("/blocking/{operation}/store", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun blockingWithCoroutineStore(@PathVariable operation: String): ResponseEntity<String> {
        val result: String = runBlocking {
            prepareWebCall(operation)
                .awaitExchange { r ->
                    if (r.statusCode().is2xxSuccessful) {
                        r.awaitBody<String>()
                    } else {
                        ""
                    }
                }
        }

        return if (result.isEmpty()) {
            ResponseEntity.notFound().build()
        } else {
            val id: Int? = runBlocking {
                storage.insertWithCoroutine(result)
            }
            ResponseEntity.ok("id $id")

        }
    }

    private final fun prepareWebCall(operation: String) = webClient
        .get()
        .uri("http://localhost:9090/$operation")
        .accept(MediaType.APPLICATION_JSON)

}

@ResponseStatus(
    value = HttpStatus.NOT_FOUND,
    reason = "Coin not found"
)
class CoinNotFoundError(message: String?) : Exception(message)
