package hello;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

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
		container.addMessageListener(new Receiver(), new PatternTopic("chat"));
		return container;
	}
	
	@Bean
	StringRedisTemplate template(JedisConnectionFactory connectionFactory) {
		return new StringRedisTemplate(connectionFactory);
	}
	
	public static void main(String[] args) throws InterruptedException {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Application.class);
		System.out.println("Waiting five seconds...");
		Thread.sleep(5000);
		StringRedisTemplate template = ctx.getBean(StringRedisTemplate.class);
		System.out.println("Sending message...");
		template.convertAndSend("chat", "Hello from Redis!");
		ctx.close();
	}
	
}
