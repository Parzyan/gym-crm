package com.company.gym.service;

import com.company.gym.entity.Credentials;

public interface AuthenticationService {
    void authenticate(Credentials credentials);
}
