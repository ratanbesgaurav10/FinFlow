package com.finflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.dto.request.AuthRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/auth/register - should register a new user")
    void register_ShouldReturn201_WhenValidRequest() throws Exception {
        AuthRequest.Register request = new AuthRequest.Register();
        request.setFullName("Test User");
        request.setEmail("testuser@finflow.com");
        request.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.email").value("testuser@finflow.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login - should login seeded admin user")
    void login_ShouldReturn200_WhenValidCredentials() throws Exception {
        AuthRequest.Login request = new AuthRequest.Login();
        request.setEmail("alice@finflow.com");
        request.setPassword("Alice@1234");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists());
    }

    @Test
    @DisplayName("POST /api/auth/login - should return 401 for wrong password")
    void login_ShouldReturn401_WhenInvalidCredentials() throws Exception {
        AuthRequest.Login request = new AuthRequest.Login();
        request.setEmail("alice@finflow.com");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/register - should return 400 for invalid email")
    void register_ShouldReturn400_WhenInvalidEmail() throws Exception {
        AuthRequest.Register request = new AuthRequest.Register();
        request.setFullName("Test User");
        request.setEmail("not-an-email");
        request.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
