import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.4.4"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.4.31"
	kotlin("plugin.spring") version "1.4.31"
	kotlin("plugin.serialization") version "1.4.31"
}

group = "goa"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

dependencies {
	val resilience4jVersion = "1.7.0"

	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
	implementation("io.r2dbc:r2dbc-postgresql")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
	implementation("io.netty:netty-resolver-dns-native-macos:4.1.60.Final:osx-x86_64")
	// implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j:2.0.1")
	implementation("io.github.resilience4j:resilience4j-reactor:${resilience4jVersion}")
	implementation("io.github.resilience4j:resilience4j-kotlin:$resilience4jVersion")
	implementation("io.github.resilience4j:resilience4j-all:${resilience4jVersion}")

	developmentOnly("org.springframework.boot:spring-boot-devtools")
	// runtimeOnly("io.micrometer:micrometer-registry-graphite")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
