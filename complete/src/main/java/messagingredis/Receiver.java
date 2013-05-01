package messagingredis;

import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

public class Receiver implements MessageListener {

	@Override
	public void onMessage(Message message, byte[] pattern) {
		DefaultMessage msg = (DefaultMessage)message;
		System.out.println("Received <" + msg.toString() + ">");
	}

}
