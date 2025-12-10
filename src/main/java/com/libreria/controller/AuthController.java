package com.libreria.controller;

import com.libreria.dto.AuthRequest;
import com.libreria.dto.AuthResponse;
import com.libreria.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private com.libreria.repository.UsuarioRepository usuarioRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        if (authentication.isAuthenticated()) {
            return new AuthResponse(jwtUtil.generateToken(authRequest.getUsername()));
        } else {
            throw new UsernameNotFoundException("Invalid user request !");
        }
    }

    @PostMapping("/register")
    public com.libreria.model.Usuario register(@RequestBody AuthRequest authRequest) {
        com.libreria.model.Usuario usuario = new com.libreria.model.Usuario();
        usuario.setUsername(authRequest.getUsername());
        usuario.setPassword(passwordEncoder.encode(authRequest.getPassword()));
        return usuarioRepository.save(usuario);
    }
}
