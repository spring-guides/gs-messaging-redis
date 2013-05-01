Getting Started: Messaging with Redis
=====================================

This Getting Started guide will walk you through the process of setting up a Redis key-value server and then using it to publish and subscribe for messages.

To help you get started, we've provided an initial project structure as well as the completed project for you in GitHub:

```sh
$ git clone https://github.com/springframework-meta/gs-messaging-redis
```

In the `start` folder, you'll find a bare project, ready for you to copy-n-paste code snippets from this document. In the `complete` folder, you'll find the complete project code.

Before we code a publishe and subscriber, there is some initial project setup that's required. Or, you can skip straight to the [fun part](#setting-up-a-redis-server).

Selecting Dependencies
----------------------
The sample in this Getting Started Guide will leverage Spring Data Redis and use slf4j for logging while running a Redis server. Therefore, the following dependencies are needed in the project's build configuration:

- org.springframework.data:spring-data-redis:1.0.3.RELEASE
- org.slf4j:slf4j-log4j12:1.7.5

Refer to the [Gradle Getting Started Guide]() or the [Maven Getting Started Guide]() for details on how to include these dependencies in your build.

Configuring a runnable application
----------------------------------

First of all, we need to create a basic runnable application.

```java
package messagingredis;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Application {

	public static void main(String[] args) throws InterruptedException {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config.class);
	}
}
```

This application will load an application context from the `Config` class. Let's define that next.

```java
package messagingredis;

import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
		
}
```

Our configuration can't get much simpler. We essentially don't have any components defined yet.

To finish setting things up, let's configure slf4j with some logging options in **log4j.properties**.

```text
# Set root logger level to DEBUG and its only appender to A1.
log4j.rootLogger=WARN, A1

# A1 is set to be a ConsoleAppender.
log4j.appender.A1=org.apache.log4j.ConsoleAppender

# A1 uses PatternLayout.
log4j.appender.A1.layout=org.apache.log4j.PatternLayout
log4j.appender.A1.layout.ConversionPattern=%-4r [%t] %-5p %c %x - %m%n

log4j.category.org.springframework=INFO
```

Now we can run out bare application.

```sh
$ ./gradlew run
```

We should see something like this:

```sh
0    [main] INFO  org.springframework.context.annotation.AnnotationConfigApplicationContext  - Refreshing org.springframework.context.annotation.AnnotationConfigApplicationContext@29a5469e: startup date [Wed May 01 14:15:05 CDT 2013]; root of context hierarchy
146  [main] INFO  org.springframework.beans.factory.support.DefaultListableBeanFactory  - Pre-instantiating singletons in org.springframework.beans.factory.support.DefaultListableBeanFactory@163202d6: defining beans [org.springframework.context.annotation.internalConfigurationAnnotationProcessor,org.springframework.context.annotation.internalAutowiredAnnotationProcessor,org.springframework.context.annotation.internalRequiredAnnotationProcessor,org.springframework.context.annotation.internalCommonAnnotationProcessor,config,org.springframework.context.annotation.ConfigurationClassPostProcessor.importAwareProcessor]; root of factory hierarchy
```

With all this setup, let's dive into building a real messaging application.

Setting up a Redis server
-------------------------
Before we can build our messaging application, we need to set up the server that will handle receiving and sending messages.

Redis is an open source, BSD-licensed, key-value data store. The server is freely available at <http://redis.io/download>. You can manually download it, or if happen to be using a Mac with homebrew:

```sh
$ brew install redis
```

Once you have unpacked it, you can launch it with default settings.

```sh
$ redis-server
```

You should expect something like this:

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

Creating a Redis message receiver
---------------------------------
With any messaging-based application, we need to create a receiver that will respond to published messages.

```java
package messagingredis;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

public class Receiver implements MessageListener {

	@Override
	public void onMessage(Message message, byte[] pattern) {
		System.out.println("Received <" + message.toString() + ">");
	}

}
```

We simply implement the `MessageListener` interface to handle a message when it gets pushed to us. Later on, we'll show how to register our listener. In this case, we are simply printing out the content of the message. 

> It's possible to do more with the message as well as have different handling based on the pattern of the message.

Publishing a message
--------------------
We've coded a message receiver. Now let's alter our startup `main` so that it not only creates an application context, but then sends out a single message.

```java
package messagingredis;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;

public class Application {

	public static void main(String[] args) throws InterruptedException {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config.class);
		System.out.println("Waiting five seconds...");
		Thread.sleep(5000);
		StringRedisTemplate template = ctx.getBean(StringRedisTemplate.class);
		System.out.println("Sending message...");
		template.convertAndSend("chat", "Hello from Redis!");
	}
}
```

Here we see the same code from earlier that created an annotation-based application context. Next we ask it to sleep for five seconds, ensuring that everything has started up successfully. Then we extract a `StringRedisTemplate` from the context and use it to publish a string message on channel `chat`.

`StringRedisTemplate` is a specialized version of `RedisTemplate` that is geared towards handling string-based functions. Since the message we're sending is a string, it suits our needs. For more complex message handling, we could switch to the more generic `RedisTemplate` if needed.

The method `convertAndSend` does the leg work for us of marshalling the channel and message into bytes and pushing them out to the Redis server we started earlier, using Spring's familiar template pattern, very similar to `JmsTemplate`.

Wiring up all the components
----------------------------
We have a sender and a receiver. We just need to wire up the components in the middle that will make it all happen. To do that, we need to add some components to `Config`.

```java
package messagingredis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class Config {
	
	@Bean
	JedisConnectionFactory connectionFactory() {
		return new JedisConnectionFactory();
	}

	@Bean
	RedisMessageListenerContainer container() {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer() {{
			setConnectionFactory(connectionFactory());
		}};
		container.addMessageListener(new Receiver(), new PatternTopic("chat"));
		return container;
	}
	
	@Bean
	StringRedisTemplate template() {
		return new StringRedisTemplate(connectionFactory());
	}
	
}
```

Here we can see three key components.

- `container()` creates an instance of `RedisMessageListenerContainer`. This is very similar to Spring Framework's JMS-oriented `DefaultMessageListenerContainer`. The container hooks up to the Redis server and listen for any published messages. Next it registeres an instance of `Receiver`, to which it will push any new messages.
- `template()` creates an instance of `StringRedisTemplate`, providing us the means to publish messages.
- `connectionFactory()` is responsible for creating a `JedisConnectionFactory`, needed for both the `RedisMessageListenerContainer` and `StringRedisTemplate` to reach the Redis server.

Building and Running our Application
------------------------------------

With our message sender and receiver coded, along with wiring up components in between, we can run our application.

```sh
$ ./gradlew run
```

We should see something like:

```sh
0    [main] INFO  org.springframework.context.annotation.AnnotationConfigApplicationContext  - Refreshing org.springframework.context.annotation.AnnotationConfigApplicationContext@4521cef5: startup date [Wed May 01 15:02:42 CDT 2013]; root of context hierarchy
175  [main] INFO  org.springframework.beans.factory.support.DefaultListableBeanFactory  - Pre-instantiating singletons in org.springframework.beans.factory.support.DefaultListableBeanFactory@693b004c: defining beans [org.springframework.context.annotation.internalConfigurationAnnotationProcessor,org.springframework.context.annotation.internalAutowiredAnnotationProcessor,org.springframework.context.annotation.internalRequiredAnnotationProcessor,org.springframework.context.annotation.internalCommonAnnotationProcessor,config,org.springframework.context.annotation.ConfigurationClassPostProcessor.importAwareProcessor,template,container,connectionFactory]; root of factory hierarchy
277  [main] INFO  org.springframework.context.support.DefaultLifecycleProcessor  - Starting beans in phase 2147483647
Waiting five seconds...
Sending message...
Received <Hello from Redis!>
```

Next Steps
----------
Congratulations! You just setup Redis and used it as a messaging system inside an application.