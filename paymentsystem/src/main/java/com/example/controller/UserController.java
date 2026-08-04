package com.example.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.CreateCardRequest;
import com.example.dto.CreateUserRequest;
import com.example.dto.CreateWalletRequest;
import com.example.dto.UpdateUserRequest;
import com.example.dto.UpdateUserStatusRequest;
import com.example.model.User;
import com.example.model.UserCard;
import com.example.model.UserWallet;
import com.example.service.UserService;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        try {
            User user = service.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping
    public List<User> getUsers() {
        return service.getUsers();
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<?> getUser(@PathVariable String accountNumber) {
        try {
            return ResponseEntity.ok(service.getUserByAccountNumber(accountNumber));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        }
    }

    @PutMapping("/{accountNumber}")
    public ResponseEntity<?> updateUser(
            @PathVariable String accountNumber,
            @RequestBody UpdateUserRequest request) {
        try {
            User user = service.updateUser(accountNumber, request);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PatchMapping("/{accountNumber}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable String accountNumber,
            @RequestBody UpdateUserStatusRequest request) {
        try {
            User user = service.updateUserStatus(accountNumber, request.isActive());
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/{accountNumber}/cards")
    public ResponseEntity<?> addCard(
            @PathVariable String accountNumber,
            @RequestBody CreateCardRequest request) {
        try {
            UserCard card = service.addCard(accountNumber, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(card);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/{accountNumber}/cards")
    public ResponseEntity<?> getCards(@PathVariable String accountNumber) {
        try {
            return ResponseEntity.ok(service.getCards(accountNumber));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/{accountNumber}/wallets")
    public ResponseEntity<?> addWallet(
            @PathVariable String accountNumber,
            @RequestBody CreateWalletRequest request) {
        try {
            UserWallet wallet = service.addWallet(accountNumber, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(wallet);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/{accountNumber}/wallets")
    public ResponseEntity<?> getWallets(@PathVariable String accountNumber) {
        try {
            return ResponseEntity.ok(service.getWallets(accountNumber));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        }
    }
}
