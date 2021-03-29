# Kotlin Web Service with couroutines / flux test 

## Target 
Share my finding on:

* how easy is to make SpringBoot apps non blockig
* showing you obvious advantages of this approach using gatling load test.
* demonstrating how seamless it composes with resilient approach


## Examples in the project 

* web calls using _reactor_ and _coroutine_ 
* web call and then store in non blocing way with _reactor_ and _coroutine_
* reactive approach using _resilience4j_
* load test with gatling


## Some words on _reactive_

* [Hystrix](https://github.com/Netflix/Hystrix) is no longer in active development, and is currently in maintenance mode.
* Spring Cloud Hystrix project is deprecated. So new applications should not use this project. [Resilience4j](https://resilience4j.readme.io/docs) is a new option for Spring developers to implement the circuit breaker pattern.
[Nice reading on infoq.com](https://www.infoq.com/articles/spring-cloud-hystrix/)
* [Istio](https://istio.io) is an intresting alternative how to make apps reactive in non programatic way

You must decide how to make apps resilient. Do you want use programatic approach with resilience4j? Should istio Traffic Management do the job? Or maybe mixed approach is the right one for your use case? 


## Getting Started

There are 4 directories:

* `sample-http-server` : mock web server; simulates http server; works on port 9090 (fast, you can set up throttle)
* `postgres-docker` : postgres in the docker
* `kotlin-flux-sandbox` : app which communicates with _postgres_ & _sample-http-server_
* `gatling-gradle-load` : load tests;works against _kotlin-flux-sandbox_

![Setup](misc/setup.png)

SpringBoot endpoints:

| Endpoint       | Description           |
|------------- |---------------| 
| `/db/all`| all entries from DB | 
| `/db/clear` | clear all entries in DB | 
| `/fill-with-test/{howMany}`| filles DB with given amount of test entries | 
| `/blocking/{operation}`| executes blocking web call to mock server | 
| `/flux/{operation}`| executes non blocking web call to mock server using _reactor_| 
| `/coroutine/{operation}`| executes non blocking web call to mock server using _couroutines_| 
| `/blocking/{operation}/store`| executes blocking web call to mock server and stores result in DB| 
| `/flux/{operation}/store`| executes non blocking web call to mock server and stores result in DB using reactive driver and _flux_| 
| `/coroutine/{operation}/store`| executes non blocking web call to mock server and stores result in DB using reactive driver and _coroutines_| 
| `/flux/resilient` | resilient4j with _reactor_ | 
| `/coroutine/resilient` | resilient4j with _coroutines_ | 
| `/mixed/resilient` | resilient4j with _reactor & coroutines_ | 

Operations on mock web server:

| Endpoint (operation)       | Description           |
|------------- |---------------| 
| `small-json`| content length 41 bytes, JSON, throttle 2sec | 
| `long-json`| content length 7431 bytes, JSON, throttle 69 millis | 

#### Technicals:
* kotlin : 1.4.31
* java 11.0.10.9.1-amzn

### Run all
#### STEP 1: POSTGRES DB
1. go to `docker` directory
1. start docker: `docker-compose up -d`
1. start bash: `docker-compose run database bash`
1. Login to psql (pass: _magical_password_): `psql --host=database --username=unicorn_user --dbname=rainbow_database`
1. Create table _texts_: `CREATE TABLE texts(id serial PRIMARY KEY, some_text VARCHAR(500) NOT NULL)`

#### STEP 2: SAMPLE WEB SERVER
1. go to `sample-http-server` directory
1. start server: `gradle run`

#### STEP 3: KOTLIN WEB APP
1. go to `kotlin-flux-sandbox` directory
1. start server: `./gradlew bootRun`
1. test if all works:  `curl -v "localhost:8080/db/all"`

#### STEP 4: Stress test it!
1. go to `gatling-gradle-plugin-demo` directory
1. start server: `./gradlew clean gatlingRun`


#### (optinal) Delete postgres data 
1. go to `docker` directory
1. execute `docker-compose down --volumes`

#### Some postgres useful commands
```
\dt+ # list all tables with details
\d table name # table details

```

## Conclusion

* Coroutines uses imperative stype therefore easy to use and learn
* Coroutines are build in in kotlin ( native support )
* Rector uses functional, streamming like, programming style which could be nicer to read
* Reactor & coroutines performance is similar depends on settings. Without tweaking coroutines seems to be faster
* Reactor is **much harder to tweak** as it uses thread pool behimd the scene 
* Reactor documentation is ( still ) better
* Switch between _coroutines_, _reactor_, _RxJava_, _CompletableFuture_ is very easy - you can mix it easily
