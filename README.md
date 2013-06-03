# Getting Started: Messaging with Redis

What you'll build
-----------------

This guide walks you through the process of publishing and subscribing to messages sent via Redis with Spring.

What you'll need
----------------

 - About 15 minutes
 - A favorite text editor or IDE
 - [JDK 6][jdk] or better
 - [Maven 3.0][mvn] or later

[jdk]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[mvn]: http://maven.apache.org/download.cgi
 - Redis server (installation instructions below)

How to complete this guide
--------------------------

Like all Spring's [Getting Started guides](/getting-started), you can start from scratch and complete each step, or you can bypass basic setup steps that are already familiar to you. Either way, you end up with working code.

To **start from scratch**, move on to [Set up the project](#scratch).

To **skip the basics**, do the following:

 - [Download][zip] and unzip the source repository for this guide, or clone it using [git](/understanding/git):
`git clone https://github.com/springframework-meta/{@project-name}.git`
 - cd into `{@project-name}/initial`
 - Jump ahead to [Create a resource representation class](#initial).

**When you're finished**, you can check your results against the code in `{@project-name}/complete`.

<a name="scratch"></a>
Set up the project
------------------

First you set up a basic build script. You can use any build system you like when building apps with Spring, but the code you need to work with [Maven](https://maven.apache.org) and [Gradle](http://gradle.org) is included here. If you're not familiar with either, refer to our [Getting Started with Maven](../gs-maven/README.md) or [Getting Started with Gradle](../gs-gradle/README.md) guides.

### Create the directory structure

In a project directory of your choosing, create the following subdirectory structure; for example, with `mkdir -p src/main/java/hello` on *nix systems:

    └── src
        └── main
            └── java
                └── hello

### Create a Maven POM

> **ERROR:** Section 'maven-project-setup-options' not found

`pom.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.springframework</groupId>
    <artifactId>gs-messaging-redis</artifactId>
    <version>0.1.0</version>

    <parent>
        <groupId>org.springframework.bootstrap</groupId>
        <artifactId>spring-bootstrap-starters</artifactId>
        <version>0.5.0.BUILD-SNAPSHOT</version>
    </parent>

    <dependencies>
        <dependency>
            <groupId>org.springframework.data</groupId>
            <artifactId>spring-data-redis</artifactId>
            <version>1.0.3.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
        </dependency>
    </dependencies>

    <properties>
        <start-class>hello.Application</start-class>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
    
    <!-- TODO: remove once bootstrap goes GA -->
    <repositories>
        <repository>
            <id>spring-snapshots</id>
            <name>Spring Snapshots</name>
            <url>http://repo.springsource.org/snapshot</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>
    <pluginRepositories>
        <pluginRepository>
            <id>spring-snapshots</id>
            <name>Spring Snapshots</name>
            <url>http://repo.springsource.org/snapshot</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>
</project>
```

TODO: mention that we're using Spring Bootstrap's [_starter POMs_](../gs-bootstrap-starter) here.

Note to experienced Maven users who are unaccustomed to using an external parent project: you can take it out later, it's just there to reduce the amount of code you have to write to get started.

### Installing and running Redis
Before we can build our messaging application, we need to set up the server that will handle receiving and sending messages.

Redis is an open source, BSD-licensed, key-value data store. The server is freely available at <http://redis.io/download>. You can manually download it, or if happen to be using a Mac with homebrew:

    $ brew install redis

Once you have unpacked it, you can launch it with default settings.

    $ redis-server

You should expect something like this:

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


<a name="initial"></a>
Creating a Redis message receiver
---------------------------------
In any messaging-based application, there are message publishers and messing receivers. To create the message receiver, you'll need to implement the `MessageListener` interface:

`src/main/java/hello/Receiver.java`
```java
package hello;

public class Receiver {
    public void receiveMessage(String message) {
        System.out.println("Received <" + message + ">");
    }
}
```

The `Receiver` is a simple POJO that defines a method for receiving messages. As you'll see when you register the `Receiver` as a message listener, the message-handling method doesn't need to be given any specific name; you can name it whatever you want.


Registering the listener and sending a message
----------------------------------------------

Spring Data Redis provides all of the components you'll need to send and receive messages with Redis. Specifically, you need to configure:

 - A connection factory
 - A message listener container
 - A Redis template

You'll use the Redis template to send messages and you will register the `Receiver` with the message listener container so that it will receive messages. The connection factory drives both the template and the message listener container, enabling them to connect to the Redis server.

`src/main/java/hello/Application.java`
```java
package hello;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class Application {
    @Bean
    JedisConnectionFactory connectionFactory() {
        return new JedisConnectionFactory();
    }
    
    @Bean
    RedisMessageListenerContainer container(final JedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer() {{
            setConnectionFactory(connectionFactory);
        }};
        container.addMessageListener(listenerAdapter(), new PatternTopic("chat"));
        return container;
    }
    
    @Bean
    MessageListenerAdapter listenerAdapter() {
        MessageListenerAdapter adapter = new MessageListenerAdapter(new Receiver());
        adapter.setDefaultListenerMethod("receiveMessage");
        return adapter;
    }
    
    @Bean
    StringRedisTemplate template(JedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
    
    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Application.class);
        StringRedisTemplate template = ctx.getBean(StringRedisTemplate.class);
        System.out.println("Sending message...");
        template.convertAndSend("chat", "Hello from Redis!");
        ctx.close();
    }
}
```

This example sets up a `JedisConnectionFactory`, a Redis connection factory based on the [Jedis](https://github.com/xetorthio/jedis) Redis library. That connection factory is injected into both the message listener container and the Redis template.

The bean defined in the `listenerAdapter()` method is registered as a message listener in the message listener container defined in `container()` and will listen for messages on the "chat" topic. Since the `Receiver` class is a POJO, it needs to be wraped in a message listener adapter that implements the `MessageListener` interface required by `addMessageListener()`. The message listener adapter is also configured to know to call the `receiveMessage()` method on `Receiver` when a message arrives.

The connection factory and message listener container beans are all you need to listen for messages. To send a message you'll also need a Redis template. Here, it is a bean configured as a `StringRedisTemplate`, an implementation of `RedisTemplate` that is focused on the common use of Redis where both keys and values are `String`s.

The `main()` method kicks everything off by creating a Spring application context. This will start the message listener container and start listening for messages. It then retrieves the `StringRedisTemplate` bean from the application context and uses it to send a "Hello from Redis!" message on the "chat" topic. Finally, it closes the Spring application context and the application ends.

### Build an executable JAR

Now that your `Application` class is ready, you simply instruct the build system to create a single, executable jar containing everything. This makes it easy to ship, version, and deploy the service as an application throughout the development lifecycle, across different environments, and so forth.

Add the following configuration to your existing Maven POM:

`pom.xml`
```xml
    <properties>
        <start-class>hello.Application</start-class>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
```

The `start-class` property tells Maven to create a `META-INF/MANIFEST.MF` file with a `Main-Class: hello.Application` entry. This entry enables you to run the jar with `java -jar`.

The [Maven Shade plugin][maven-shade-plugin] extracts classes from all jars on the classpath and builds a single "über-jar", which makes it more convenient to execute and transport your service.

Now run the following to produce a single executable JAR file containing all necessary dependency classes and resources:

    mvn package

[maven-shade-plugin]: https://maven.apache.org/plugins/maven-shade-plugin

Run the application
-------------------
Run your application with `java -jar` at the command line:

    java -jar target/gs-messaging-redis-0.1.0.jar


You should see the following output:

    Sending message...
    Received <Hello from Redis!>

Summary
-------
Congrats! You've just developed a simple publisher and subscriber application using Spring and Redis. There's more you can do with Spring and Redis than what is covered here, but this should provide a good start.
