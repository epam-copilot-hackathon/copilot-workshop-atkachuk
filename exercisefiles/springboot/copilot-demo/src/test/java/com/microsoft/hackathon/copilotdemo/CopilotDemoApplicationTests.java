package com.microsoft.hackathon.copilotdemo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.hamcrest.Matchers;
import org.springframework.test.web.servlet.MvcResult;



@SpringBootTest()
@AutoConfigureMockMvc 
class CopilotDemoApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
	void hello() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/hello?key=world"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().string("hello world"));
	}

	@Test
	void diffDates() throws Exception {
		String date1 = "01-01-2022";
		String date2 = "10-01-2022";

		mockMvc.perform(MockMvcRequestBuilders.get("/diffdates?date1=" + date1 + "&date2=" + date2))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().string("9"));
	}

	@Test
	void validatePhoneNumber() throws Exception {
		String validPhoneNumber = "+34600000000";
		String invalidPhoneNumber = "+35100000000";

		mockMvc.perform(MockMvcRequestBuilders.get("/validatePhoneNumber?phoneNumber=" + validPhoneNumber))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().string("true"));

		mockMvc.perform(MockMvcRequestBuilders.get("/validatePhoneNumber?phoneNumber=" + invalidPhoneNumber))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().string("false"));
	}

	@Test
	void validateDNI() throws Exception {
		String validDNI = "12345678Z";
		String invalidDNI = "1234567Z";

		mockMvc.perform(MockMvcRequestBuilders.get("/validateDNI?dni=" + validDNI))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().string("true"));

		mockMvc.perform(MockMvcRequestBuilders.get("/validateDNI?dni=" + invalidDNI))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().string("false"));
	}

	// @Test
	// void getColorHexCode() throws Exception {
	// 	String validColorName = "red";
	// 	String invalidColorName = "notacolor";

	// 	mockMvc.perform(MockMvcRequestBuilders.get("/color/" + validColorName))
	// 		.andExpect(MockMvcResultMatchers.status().isOk())
	// 		.andExpect(MockMvcResultMatchers.content().string("#FF0000"));

	// 	mockMvc.perform(MockMvcRequestBuilders.get("/color/" + invalidColorName))
	// 		.andExpect(MockMvcResultMatchers.status().isNotFound());
	// }

	@Test
	void getJoke() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/joke"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.isEmptyString())));
	}

	@Test
    void parseUrl() throws Exception {
        String validUrl = "https://www.example.com:8080/test/path?param1=value1%26param2=value2";
        mockMvc.perform(MockMvcRequestBuilders.get("/parseUrl?url=" + validUrl))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.protocol", Matchers.is("https")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.authority", Matchers.is("www.example.com:8080")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.host", Matchers.is("www.example.com")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.port", Matchers.is("8080")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.path", Matchers.is("/test/path")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.query", Matchers.is("param1=value1&param2=value2")));

        String invalidUrl = "not a url";
        mockMvc.perform(MockMvcRequestBuilders.get("/parseUrl?url=" + invalidUrl))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.error", Matchers.is("Invalid URL")));
    }

	@Test
	void listFilesAndFolders() throws Exception {
		String path = "src/main/resources";
		mockMvc.perform(MockMvcRequestBuilders.get("/listFilesAndFolders?path=" + path))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.isEmptyString())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.files", Matchers.hasSize(3)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.folders", Matchers.hasSize(0)));
	}

	@Test
	void testWordCount() throws Exception {
		String path = "src/main/resources/wordCount.txt";
		String word = "test";

		mockMvc.perform(MockMvcRequestBuilders.get("/wordCount?path=" + path + "&word=" + word))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$." + word, Matchers.is(2)));
	}

	@Test
	void testZipFolder() throws Exception {
		String path = "src/main/resources";
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/zipFolder?path=" + path))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andReturn();

		// Check that the response is a zip file
		String contentType = result.getResponse().getContentType();
		assertEquals("application/octet-stream", contentType);

		// Check that the content is not empty
		byte[] responseBytes = result.getResponse().getContentAsByteArray();
		assertTrue(responseBytes.length > 0);
	}



}