package com.crud.rental.service;

import com.crud.rental.domain.User;
import com.crud.rental.exception.UserNotFoundException;
import com.crud.rental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private User loggedUser;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    public User getUserById(final Long userId) throws UserNotFoundException{
      return   userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    }
    public void createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }
    public void deleteUser(final Long userId) throws UserNotFoundException{
        userRepository.delete(userRepository.findById(userId).orElseThrow(UserNotFoundException::new));
    }
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void setAdmin(Long userId, boolean admin) throws UserNotFoundException {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        user.setAdmin(admin);
        userRepository.save(user);
    }

    public void login(User user) {
        // Logika logowania
        // Po udanym logowaniu ustaw `loggedInUser`
        loggedUser = user;
    }

    public User getLoggedUser() {
        return loggedUser;
    }

}
