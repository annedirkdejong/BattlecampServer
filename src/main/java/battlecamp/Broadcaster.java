package battlecamp;

import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.core.MessageSendingOperations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Broadcaster {
	private MessageSendingOperations<String> messageSendingOperations;
	private JmsTemplate jmsTemplate;

	public Broadcaster(MessageSendingOperations<String> messageSendingOperations, JmsTemplate jmsTemplate){
		this.messageSendingOperations = messageSendingOperations;
		this.jmsTemplate = jmsTemplate;
		
	}
	
	public void broadcast(String topic,Object object){
		messageSendingOperations.convertAndSend("/topic/"+topic, object);
		try {
			this.jmsTemplate.convertAndSend(topic, new ObjectMapper().writeValueAsString(object));
		} catch (JmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
