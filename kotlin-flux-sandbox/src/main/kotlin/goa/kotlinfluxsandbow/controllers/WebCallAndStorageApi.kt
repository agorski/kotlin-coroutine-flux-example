package goa.kotlinfluxsandbow.controllers

import goa.kotlinfluxsandbow.Storage
import goa.kotlinfluxsandbow.Texts
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.kotlin.circuitbreaker.circuitBreaker
import io.github.resilience4j.kotlin.circuitbreaker.executeSuspendFunction
import io.github.resilience4j.kotlin.timelimiter.executeSuspendFunction
import io.github.resilience4j.kotlin.timelimiter.timeLimiter
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator
import io.github.resilience4j.timelimiter.TimeLimiter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.util.*


@Suppress("unused")
@RestController
class WebCallAndStorageApi(val storage: Storage) {
    private val circuitBreaker = CircuitBreaker.of(
        "cb",
        io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
            .minimumNumberOfCalls(2)
            .waitDurationInOpenState(Duration.ofMillis(10000))
            .slowCallDurationThreshold(Duration.ofSeconds(2))
            .permittedNumberOfCallsInHalfOpenState(3)
            .slidingWindowType(io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(5)
            .permittedNumberOfCallsInHalfOpenState(2)
            .build()
    )

    private val timeLimiter = TimeLimiter.of(
        "timelimiter named",
        io.github.resilience4j.timelimiter.TimeLimiterConfig.custom().timeoutDuration(Duration.ofMillis(100)).build()
    )

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

    @GetMapping("/flux/resilient", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun withFluxAndResilient(): Flux<String> {
        val exchangeToFlow: Flux<String> = prepareWebCall("long-json").exchangeToFlux {
            Flux.error(java.lang.Exception("forced exception"))
        }
        return exchangeToFlow.transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
    }

    @GetMapping("/mixed/resilient", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun withFluxCircuitMixed(): Flow<String> {
        val exchangeToFlow: Flux<String> = prepareWebCall("slow-json").exchangeToFlux { response ->
            val statusCode = response.statusCode()
            when {
                statusCode.is2xxSuccessful -> {
                    response.bodyToFlux(String::class.java)
                }
                statusCode.is4xxClientError -> {
                    response.bodyToMono(String::class.java).flux()
                }
                else -> {
                    Flux.error(java.lang.Exception("Unexpected response status $statusCode"))
                }
            }

        }
        return exchangeToFlow
            .asFlow()
            .timeLimiter(timeLimiter)
            .circuitBreaker(circuitBreaker)
    }

    @GetMapping("/coroutine/resilient", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun withFluxResilience(): String {
        return circuitBreaker.executeSuspendFunction {
            timeLimiter.executeSuspendFunction {
                prepareWebCall("slow-json")
                    .awaitExchange { clientResponse ->
                        val statusCode = clientResponse.statusCode()
                        if (statusCode.is2xxSuccessful) {
                            clientResponse.awaitBody()
                        } else {
                            throw Exception("Unexpected response status $statusCode")
                        }

                    }
            }
        }
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
