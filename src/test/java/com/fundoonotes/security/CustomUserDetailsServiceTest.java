package com.fundoonotes.security;

import com.fundoonotes.entity.Role;
import com.fundoonotes.entity.User;
import com.fundoonotes.enums.RoleName;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.security.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {
    private UserRepository userRepository;
    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        service = new CustomUserDetailsService(userRepository);
    }

    @Test
    void shouldLoadUserFromUsername() {
        User user = User.builder()
                .username("alice")
                .email("alice@mail.com")
                .password("hash")
                .emailVerified(true)
                .roles(Set.of(Role.builder().name(RoleName.ROLE_USER).build()))
                .build();
        when(userRepository.findByEmail("alice")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        var details = service.loadUserByUsername("alice");
        assertEquals("alice", details.getUsername());
        assertFalse(details.getAuthorities().isEmpty());
    }

    @Test
    void shouldThrowWhenUserMissing() {
        when(userRepository.findByEmail("x")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("x")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("x"));
    }
}
