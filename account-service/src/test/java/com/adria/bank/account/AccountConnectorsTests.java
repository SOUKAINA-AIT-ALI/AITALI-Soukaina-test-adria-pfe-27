package com.adria.bank.account;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AccountConnectorsTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnAccountsViaRest() throws Exception {
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$[0].currency", is("MAD")));
    }

    @Test
    void shouldReturnSingleAccountViaRest() throws Exception {
        mockMvc.perform(get("/api/accounts/11111111-1111-1111-1111-111111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerReference", is("CUST-1001")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    void shouldListMcpTools() throws Exception {
        mockMvc.perform(get("/mcp/tools"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].name", is("getAccountBalance")));
    }

    @Test
    void shouldExecuteMcpGetAccountBalance() throws Exception {
        mockMvc.perform(post("/mcp/tools/getAccountBalance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountId\": \"11111111-1111-1111-1111-111111111111\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", containsString("5000")));
    }
}

