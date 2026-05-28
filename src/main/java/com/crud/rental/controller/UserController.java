package com.crud.rental.controller;

import com.crud.rental.domain.LoginRequest;
import com.crud.rental.domain.RegisterRequest;
import com.crud.rental.domain.User;
import com.crud.rental.domain.UserDto;
import com.crud.rental.exception.UserNotFoundException;
import com.crud.rental.mapper.UserMapper;
import com.crud.rental.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userMapper.mapToUserDtoList(userService.getAllUsers()));
    }

    private static final java.util.regex.Pattern USERNAME_PATTERN =
            java.util.regex.Pattern.compile("^[a-zA-Z0-9_\\-]{3,30}$");

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody RegisterRequest request) {
        if (!USERNAME_PATTERN.matcher(request.getUsername()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Username may only contain letters, digits, _ and - (3–30 chars)");
        }
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setAdmin(false);
        userService.createUser(user);
        User saved = userService.findByUsername(request.getUsername());
        return ResponseEntity.ok(userMapper.mapToUserDto(saved));
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody LoginRequest loginRequest) {
        User user = userService.findByUsername(loginRequest.getUsername());
        if (user == null || !user.getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(userMapper.mapToUserDto(user));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) throws UserNotFoundException {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }
}
