package dev.portfolio.confirm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class ConfirmationTests {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ConfirmationRepository confirmations;

	@Test
	void theTokenIsReturnedOnceAndOnlyItsHashIsStored() throws Exception {
		MvcResult created = mvc.perform(post("/api/confirmations")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"Ada@Example.com\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value("ada@example.com"))
				.andReturn();
		String token = JsonPath.read(created.getResponse().getContentAsString(), "$.token");

		Confirmation stored = confirmations.findAll().stream()
				.filter(row -> row.getEmail().equals("ada@example.com"))
				.findFirst()
				.orElseThrow();
		assertThat(stored.getTokenHash()).isEqualTo(ConfirmationService.sha256(token));
		assertThat(stored.getTokenHash()).isNotEqualTo(token);

		mvc.perform(post("/api/confirmations/confirm")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"token\":\"" + token + "\"}"))
				.andExpect(status().isNoContent());

		Confirmation confirmed = confirmations.findAll().stream()
				.filter(row -> row.getEmail().equals("ada@example.com"))
				.findFirst()
				.orElseThrow();
		assertThat(confirmed.getTokenHash()).isNull();
		assertThat(confirmed.getStatus()).isEqualTo(Confirmation.Status.CONFIRMED);

		mvc.perform(post("/api/confirmations/confirm")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"token\":\"" + token + "\"}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void anUnknownTokenIsNotFound() throws Exception {
		mvc.perform(post("/api/confirmations/confirm")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"token\":\"missing-token-value\"}"))
				.andExpect(status().isNotFound());
	}
}
