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
		return new RedisMessageListenerContainer() {{
			setConnectionFactory(connectionFactory());
		}};
	}
	
	@Bean
	Receiver receiver() {
		Receiver receiver = new Receiver();
		container().addMessageListener(receiver, new PatternTopic("chat"));
		return receiver;
	}
	
	@Bean
	StringRedisTemplate template() {
		return new StringRedisTemplate(connectionFactory());
	}
	
}
