package hello;

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
