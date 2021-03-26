package goa.kotlinfluxsandbow

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.spi.ConnectionFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration

@SpringBootApplication
class KotlinFluxSandbowApplication

fun main(args: Array<String>) {
	runApplication<KotlinFluxSandbowApplication>(*args)
}

@Configuration
class ApplicationConfiguration(private val env: Environment) : AbstractR2dbcConfiguration() {

	@Override
	@Bean
	override fun connectionFactory(): ConnectionFactory {
		return PostgresqlConnectionFactory(
			PostgresqlConnectionConfiguration.builder()
				.host(env.getProperty("spring.r2dbc.host", ""))
				.database(env.getProperty("spring.r2dbc.database", ""))
				.username(env.getProperty("spring.r2dbc.username", ""))
				.password(env.getProperty("spring.r2dbc.password", ""))
				.build()
		)
	}
}

