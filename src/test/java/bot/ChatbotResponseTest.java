package bot;

import static org.junit.Assert.*;

import org.junit.Test;

import model.ChatbotResponse;

public class ChatbotResponseTest {

	@Test
	public void testSimpleChatbotResponse(){
		String expectedReply = "Test";
		
		// Action
		ChatbotResponse chatbotResponse = new ChatbotResponse(expectedReply);
		
		// Check
		assertEquals(expectedReply,chatbotResponse.getReply());
		assertTrue(chatbotResponse.getKeyboardButtons().isEmpty());
		assertFalse(chatbotResponse.hasChangedKeyboard());	
	}
	
	@Test
	public void testChatbotResponse(){
		// Prepare
		String reply = "Test";
		String keyboardButton1 = "New Button";
		String keyboardButton2 = "New Button 2";
		
		// Action
		ChatbotResponse chatbotResponse = new ChatbotResponse(reply, keyboardButton1, keyboardButton2);
		
		// Check
		assertEquals(reply,chatbotResponse.getReply());
		assertTrue(chatbotResponse.hasChangedKeyboard());
		assertTrue(chatbotResponse.getKeyboardButtons().contains(keyboardButton1));
		assertTrue(chatbotResponse.getKeyboardButtons().contains(keyboardButton2));
	}
	
	@Test
	public void testSendPhoto(){
		String reply = "https://igx.4sqi.net/img/general/width200/3877816_8U-mGuA-llrxlNX_44keCtkMcBcDvanjh4jxCBN3Mwo.jpg";
		
		// Action
		ChatbotResponse chatbotResponse = new ChatbotResponse(reply);
		
		// Check
		assertEquals(reply,chatbotResponse.getReply());
		assertFalse(chatbotResponse.hasChangedKeyboard());
		assertTrue(chatbotResponse.hasPhoto());
	
	}
}
