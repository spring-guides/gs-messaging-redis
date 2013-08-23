<#assign project_id="gs-messaging-redis">
This guide walks you through the process of using Spring Data Redis to publish and subscribe to messages sent via Redis.

What you'll build
-----------------

You'll build an application that uses `StringRedisTemplate` to publish a string message and has a POJO subscribe for it using `MessageListenerAdapter`.

> **Note:** It may sound strange to be using Spring Data Redis as the means to publish messages, but as you'll discover, Redis not only provides a NoSQL data store, but a messaging system as well.


What you'll need
----------------

 - About 15 minutes
 - <@prereq_editor_jdk_buildtools/>
 - Redis server (installation instructions below)

## <@how_to_complete_this_guide jump_ahead='Create a Redis message receiver'/>

<a name="scratch"></a>
Set up the project
------------------

<@build_system_intro/>

<@create_directory_structure_hello/>


<@create_both_builds/>

<@bootstrap_starter_pom_disclaimer/>

### Install and run Redis

Before you can build a messaging application, you need to set up the server that will handle receiving and sending messages.

Redis is an open source, BSD-licensed, key-value data store that also comes with a messaging system. The server is freely available at <http://redis.io/download>. You can download it manually, or if you use a Mac with homebrew:

```sh
$ brew install redis
```

Once you unpack Redis, you can launch it with default settings.

```sh
$ redis-server
```

You should see a message like this:

```sh
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
  |    `-._`-._        _.-'_.-'    |           http://redis.io
    `-._    `-._`-.__.-'_.-'    _.-'
  |`-._`-._    `-.__.-'    _.-'_.-'|
  |    `-._`-._        _.-'_.-'    |
    `-._    `-._`-.__.-'_.-'    _.-'
        `-._    `-.__.-'    _.-'
            `-._        _.-'
                `-.__.-'

[35142] 01 May 14:36:28.941 # Server started, Redis version 2.6.12
[35142] 01 May 14:36:28.941 * The server is now ready to accept connections on port 6379
```


<a name="initial"></a>
Create a Redis message receiver
-------------------------------

In any messaging-based application, there are message publishers and messaging receivers. To create the message receiver, implement a receiver with a method to respond to messages:

    <@snippet path="src/main/java/hello/Receiver.java" prefix="complete"/>

The `Receiver` is a simple POJO that defines a method for receiving messages. As you'll see when you register the `Receiver` as a message listener, you can name the message-handling method whatever you want.


Register the listener and send a message
----------------------------------------

Spring Data Redis provides all the components you need to send and receive messages with Redis. Specifically, you need to configure:

 - A connection factory
 - A message listener container
 - A Redis template

You'll use the Redis template to send messages and you will register the `Receiver` with the message listener container so that it will receive messages. The connection factory drives both the template and the message listener container, enabling them to connect to the Redis server.

This example sets up a `JedisConnectionFactory`, a Redis connection factory based on the [Jedis](https://github.com/xetorthio/jedis) Redis library. That connection factory is injected into both the message listener container and the Redis template.

    <@snippet path="src/main/java/hello/Application.java" prefix="complete"/>

The bean defined in the `listenerAdapter()` method is registered as a message listener in the message listener container defined in `container()` and will listen for messages on the "chat" topic. Because the `Receiver` class is a POJO, it needs to be wrapped in a message listener adapter that implements the `MessageListener` interface required by `addMessageListener()`. The message listener adapter is also configured to call the `receiveMessage()` method on `Receiver` when a message arrives.

The connection factory and message listener container beans are all you need to listen for messages. To send a message you also need a Redis template. Here, it is a bean configured as a `StringRedisTemplate`, an implementation of `RedisTemplate` that is focused on the common use of Redis where both keys and values are `String`s.

The `main()` method kicks everything off by creating a Spring application context. The application context then starts the message listener container, and the message listener container bean starts listening for messages. The `main()` method then retrieves the `StringRedisTemplate` bean from the application context and uses it to send a "Hello from Redis!" message on the "chat" topic. Finally, it closes the Spring application context and the application ends.

<@build_an_executable_jar_mainhead/>
<@build_an_executable_jar_with_both/>


<@run_the_application_with_both/>


You should see the following output:

    Sending message...
    Received <Hello from Redis!>


Summary
-------
Congratulations! You've just developed a simple publish-and-subscribe application with Spring and Redis. 

