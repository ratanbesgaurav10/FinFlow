package com.finflow.service;

import com.finflow.dto.request.AuthRequest;
import com.finflow.dto.response.ResponseDTOs;

public interface AuthService {
    ResponseDTOs.AuthResponse register(AuthRequest.Register request);
    ResponseDTOs.AuthResponse login(AuthRequest.Login request);
}
