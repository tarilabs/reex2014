package net.tarilabs.reex2014;

import static org.junit.Assert.*;

import java.net.URI;

import org.junit.Test;

public class TestCamelRoutesAPI {

	@Test
	public void testformatInputFromDefinition() {
		String camelFrom = "twitter://timeline/user?user="
				+ "twitter_user"
				+ "&type=polling&delay=60"
				+ "&consumerKey=" + "aaa"
				+ "&consumerSecret=" + "bbb"
				+ "&accessToken=" + "ccc"
				+ "&accessTokenSecret=" + "ddd";
		
		String formatted = CamelRoutesAPI.formatInputFromURI(URI.create(camelFrom));
		System.out.println(formatted);
		assertFalse(formatted.contains("aaa"));
		assertFalse(formatted.contains("bbb"));
		assertFalse(formatted.contains("ccc"));
		assertFalse(formatted.contains("ddd"));
	}
}
