package com.faris.enterprise_order_api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EnterpriseOrderApiApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void helloReturnsExpectedResponse() throws Exception {
		mockMvc.perform(get("/api/v1/hello"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Hello, Enterprise Java!"))
				.andExpect(jsonPath("$.application").value("Enterprise Order API"))
				.andExpect(jsonPath("$.version").value("1.0.0"));
	}

	@Test
	void statusReturnsExpectedResponse() throws Exception {
		mockMvc.perform(get("/api/v1/status"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.application").value("Enterprise Order API"))
				.andExpect(jsonPath("$.version").value("1.0.0"));
	}

}
