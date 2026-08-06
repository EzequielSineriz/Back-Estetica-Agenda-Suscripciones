package com.AppEstetica.service.Usuarios;


import com.AppEstetica.advice.ResourceNotFoundException;
import com.AppEstetica.entities.Rol;
import com.AppEstetica.entities.User;
import com.AppEstetica.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // -------------------- FIND --------------------

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username " + username));
    }


    // -------------------- CREATE --------------------

    public User createUser(String username, String email,String rawPassword, Set<Rol> rols) {

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists: " + username);
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .roles(rols)
                .build();

        return userRepository.save(user);
    }


    // -------------------- UPDATE --------------------

    public User updateUser(Long id, String newUsername, String newPassword, Set<Rol> rols) {
        User user = findById(id);

        if (!user.getUsername().equals(newUsername)
                && userRepository.existsByUsername(newUsername)) {
            throw new RuntimeException("Username already taken: " + newUsername);
        }

        user.setUsername(newUsername);

        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        if (rols != null) {
            user.setRoles(rols);
        }

        return userRepository.save(user);
    }


    // -------------------- DELETE --------------------

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id " + id);
        }
        userRepository.deleteById(id);
    }


    // -------------------- LIST --------------------

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }



}