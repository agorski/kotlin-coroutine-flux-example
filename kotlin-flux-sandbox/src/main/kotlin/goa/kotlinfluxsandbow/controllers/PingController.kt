package goa.kotlinfluxsandbow.controllers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import java.lang.Exception
import kotlin.random.Random
import kotlin.Int as Int

@RestController
class PingController {

    @GetMapping("/single/blocking")
    fun singleBlocking(): PongResponse {
        return pongResponse()
    }

    @GetMapping("/single/flux")
    fun singleFlux(): Mono<PongResponse> {
        return Mono.just(pongResponse())
    }

    @GetMapping("/single/coroutine")
    suspend fun singleCoroutine(): PongResponse {
        return pongResponse()
    }

    @GetMapping("/single/mixed")
    @Suppress("UnnecessaryVariable")
    fun singleCallCoroutineWithResultAsMono(): Mono<PongResponse> {
        val coroutineResultAsMono: Mono<PongResponse> = mono {
            funcWithCoroutine()
        }
        return coroutineResultAsMono
    }

    private suspend fun funcWithCoroutine(): PongResponse {
        return pongResponse()
    }

    @GetMapping("/list/blocking")
    fun listBlocking(): List<PongResponse> {
        return generateList()
    }
    @GetMapping("/list/coroutine")
    suspend fun listCoroutine(): Flow<PongResponse> {
        return generateList().asFlow()
    }

    @GetMapping("/list/flux")
    fun listFlux(): Flux<PongResponse> {
        return generateList().toFlux()
    }

    private final fun generateList(): List<PongResponse> {
        return listOf(
            pongResponse(),
            PongResponse(IdGenerator.nextId(), "Second"),
            PongResponse(IdGenerator.nextId(), "Third")
        )
    }

    private fun pongResponse() = PongResponse(IdGenerator.nextId(), "First")
}

object IdGenerator {
    private val nextRandomInt = Random(1)

    fun nextId(): Int = nextRandomInt.nextInt()
}

data class PongResponse(val id: Int, val name: String = "John Doe")

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class CustomException(message: String?) : Exception(message)