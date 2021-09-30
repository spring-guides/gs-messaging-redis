:toc:
:icons: font
:source-highlighter: prettify
:project_id: gs-messaging-redis

This guide walks you through the process of using Spring Data Redis to publish and
subscribe to messages sent with Redis.

== What You Will build

You will build an application that uses `StringRedisTemplate` to publish a string message
and has a POJO subscribe for the message by using `MessageListenerAdapter`.

NOTE: It may sound strange to be using Spring Data Redis as the means to publish messages,
but, as you will discover, Redis provides not only a NoSQL data store but a messaging
system as well.


== What You Need

:java_version: 1.8
include::https://raw.githubusercontent.com/spring-guides/getting-started-macros/main/prereq_editor_jdk_buildtools.adoc[]
+
- A Redis server (See <<scratch>>)

include::https://raw.githubusercontent.com/spring-guides/getting-started-macros/main/how_to_complete_this_guide.adoc[]

[[scratch]]
== Standing up a Redis server

Before you can build a messaging application, you need to set up the server that will
handle receiving and sending messages.

Redis is an open source, BSD-licensed, key-value data store that also comes with a
messaging system. The server is freely available at https://redis.io/download. You can
download it manually, or, if you use a Mac, with Homebrew, by running the following
command in a terminal window:

====
[source,bash]
----
brew install redis
----
====

Once you unpack Redis, you can launch it with its default settings by running the following command:

====
[source,bash]
----
redis-server
----
====

You should see a message similar to the following:

====
[source,text]
----
[35142] 01 May 14:36:28.939 # Warning: no config file specified, using the default config. In order to specify a config file use redis-server /path/to/redis.conf
[35142] 01 May 14:36:28.940 * Max number of open files set to 10032
                _._
              _.-``__ ''-._
        _.-``    `.  `_.  ''-._           Redis 2.6.12 (00000000/0) 64 bit
    .-`` .-```.  ```\/    _.,_ ''-._
  (    '      ,       .-`  | `,    )     Running in stand alone mode
  |`-._`-...-` __...-.``-._|'` _.-'|     Port: 6379
  |    `-._   `._    /     _.-'    |     PID: 35142
    `-._    `-._  `-./  _.-'    _.-'
  |`-._`-._    `-.__.-'    _.-'_.-'|
  |    `-._`-._        _.-'_.-'    |           https://redis.io
    `-._    `-._`-.__.-'_.-'    _.-'
  |`-._`-._    `-.__.-'    _.-'_.-'|
  |    `-._`-._        _.-'_.-'    |
    `-._    `-._`-.__.-'_.-'    _.-'
        `-._    `-.__.-'    _.-'
            `-._        _.-'
                `-.__.-'

[35142] 01 May 14:36:28.941 # Server started, Redis version 2.6.12
[35142] 01 May 14:36:28.941 * The server is now ready to accept connections on port 6379
----
====

[[initial]]
== Starting with Spring Initializr

You can use this https://start.spring.io/#!type=maven-project&language=java&platformVersion=2.5.5&packaging=jar&jvmVersion=11&groupId=com.example&artifactId=messaging-redis&name=messaging-redis&description=Demo%20project%20for%20Spring%20Boot&packageName=com.example.messaging-redis&dependencies=data-redis[pre-initialized project] and click Generate to download a ZIP file. This project is configured to fit the examples in this tutorial.

To manually initialize the project:

. Navigate to https://start.spring.io.
This service pulls in all the dependencies you need for an application and does most of the setup for you.
. Choose either Gradle or Maven and the language you want to use. This guide assumes that you chose Java.
. Click *Dependencies* and select *Spring Data Redis*.
. Click *Generate*.
. Download the resulting ZIP file, which is an archive of a web application that is configured with your choices.

NOTE: If your IDE has the Spring Initializr integration, you can complete this process from your IDE.

NOTE: You can also fork the project from Github and open it in your IDE or other editor.

== Create a Redis Message Receiver

In any messaging-based application, there are message publishers and messaging receivers.
To create the message receiver, implement a receiver with a method to respond to messages,
as the following example (from `src/main/java/com/example/messagingredis/Receiver.java`)
shows:

====
[source,java]
----
include::complete/src/main/java/com/example/messagingredis/Receiver.java[]
----
====

The `Receiver` is a POJO that defines a method for receiving messages. When you register
the `Receiver` as a message listener, you can name the message-handling method whatever
you want.

NOTE: For demonstration purposes, the receiver is counting the messages received. That way, it can signal when it has received a message.


== Register the Listener and Send a Message

Spring Data Redis provides all the components you need to send and receive messages with
Redis. Specifically, you need to configure:

- A connection factory
- A message listener container
- A Redis template

You will use the Redis template to send messages, and you will register the `Receiver`
with the message listener container so that it will receive messages. The connection
factory drives both the template and the message listener container, letting them connect
to the Redis server.

This example uses Spring Boot's default `RedisConnectionFactory`, an instance of
`JedisConnectionFactory` that is based on the https://github.com/xetorthio/jedis[Jedis]
Redis library. The connection factory is injected into both the message listener container
and the Redis template, as the following example (from
`src/main/java/com/example/messagingredis/MessagingRedisApplication.java`) shows:

====
[source,java]
----
include::complete/src/main/java/com/example/messagingredis/MessagingRedisApplication.java[]
----
====

The bean defined in the `listenerAdapter` method is registered as a message listener in
the message listener container defined in `container` and will listen for messages on the
`chat` topic. Because the `Receiver` class is a POJO, it needs to be wrapped in a message
listener adapter that implements the `MessageListener` interface (which is required by
`addMessageListener()`). The message listener adapter is also configured to call the
`receiveMessage()` method on `Receiver` when a message arrives.

The connection factory and message listener container beans are all you need to listen for
messages. To send a message, you also need a Redis template. Here, it is a bean configured
as a `StringRedisTemplate`, an implementation of `RedisTemplate` that is focused on the
common use of Redis, where both keys and values are `String` instances.

The `main()` method kicks off everything by creating a Spring application context. The
application context then starts the message listener container, and the message listener
container bean starts listening for messages. The `main()` method then retrieves the
`StringRedisTemplate` bean from the application context and uses it to send a
`Hello from Redis!` message on the `chat` topic. Finally, it closes the Spring application
context, and the application ends.

include::https://raw.githubusercontent.com/spring-guides/getting-started-macros/main/build_an_executable_jar_mainhead.adoc[]

include::https://raw.githubusercontent.com/spring-guides/getting-started-macros/main/build_an_executable_jar_with_both.adoc[]

You should see output similar to the following:

====
[source,text]
----

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.1.8.RELEASE)

2019-09-23 12:57:11.578  INFO 35396 --- [           main] c.e.m.MessagingRedisApplication          : Starting MessagingRedisApplication on Jays-MBP with PID 35396 (/Users/j/projects/guides/gs-messaging-redis/complete/target/classes started by j in /Users/j/projects/guides/gs-messaging-redis/complete)
2019-09-23 12:57:11.581  INFO 35396 --- [           main] c.e.m.MessagingRedisApplication          : No active profile set, falling back to default profiles: default
2019-09-23 12:57:11.885  INFO 35396 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode!
2019-09-23 12:57:11.887  INFO 35396 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data repositories in DEFAULT mode.
2019-09-23 12:57:11.914  INFO 35396 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 13ms. Found 0 repository interfaces.
2019-09-23 12:57:12.685  INFO 35396 --- [    container-1] io.lettuce.core.EpollProvider            : Starting without optional epoll library
2019-09-23 12:57:12.685  INFO 35396 --- [    container-1] io.lettuce.core.KqueueProvider           : Starting without optional kqueue library
2019-09-23 12:57:12.848  INFO 35396 --- [           main] c.e.m.MessagingRedisApplication          : Started MessagingRedisApplication in 1.511 seconds (JVM running for 3.685)
2019-09-23 12:57:12.849  INFO 35396 --- [           main] c.e.m.MessagingRedisApplication          : Sending message...
2019-09-23 12:57:12.861  INFO 35396 --- [    container-2] com.example.messagingredis.Receiver      : Received <Hello from Redis!>
----
====

== Summary

Congratulations! You have just developed a publish-and-subscribe application with Spring
and Redis.

NOTE: https://pivotal.io/products/redis[Redis support] is available.

== See Also

The following guides may also be helpful:

* https://spring.io/guides/gs/messaging-rabbitmq/[Messaging with RabbitMQ]
* https://spring.io/guides/gs/messaging-jms/[Messaging with JMS]
* https://spring.io/guides/gs/spring-boot/[Building an Application with Spring Boot]

include::https://raw.githubusercontent.com/spring-guides/getting-started-macros/main/footer.adoc[]
