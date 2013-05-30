package hello;

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
