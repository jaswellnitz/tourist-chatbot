package bot;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Before;
import org.junit.Test;

import chatbot.ImageRequester;
import model.Location;

public class ImageRequesterTest {

	private ImageRequester imageRequester;

	@Before
	public void setUp(){
		this.imageRequester = new ImageRequester(System.getenv("F_CLIENT_ID"),System.getenv("F_CLIENT_SECRET"));
	}
	
	@Test
	public void testGetImageUrl(){
		String name = "Básilica de la Sagrada Família";
		Location location = new Location(41.4034984,2.1740598);
		
		String imageURL = imageRequester.getImageURL(name, location);
		System.out.println(imageURL);
		assertNotNull(imageURL);
		assertFalse(imageURL.isEmpty());
		
		try {
		    URL url = new URL(imageURL);
		    URLConnection conn = url.openConnection();
		    conn.connect();
		} catch (MalformedURLException e) {
			fail(e.getMessage());
		    // the URL is not in a valid form
		} catch (IOException e) {
			// the connection couldn't be established
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetImageUrlEmpty(){
		String name = "Casa Vella";
		Location location = new Location(41.2730278,1.880012);
		
		String imageURL = imageRequester.getImageURL(name, location);
		
		assertNotNull(imageURL);
		assertTrue(imageURL.isEmpty());
	}
}
