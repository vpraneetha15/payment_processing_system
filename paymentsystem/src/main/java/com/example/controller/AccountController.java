package com.example.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.Account;
import com.example.service.AccountService;

@RestController
@RequestMapping("/accounts")
@CrossOrigin(origins = "*")
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody Account account) {
        service.save(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping
    public ResponseEntity<List<Account>> getAccounts() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<?> getAccount(@PathVariable String accountNumber) {
        Account account = service.findById(accountNumber);
        if (account == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Account not found", "accountNumber", accountNumber));
        }
        return ResponseEntity.ok(account);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody Account account) {
        int rows = service.update(account);
        if (rows == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Account not found", "accountNumber", account.getAccountNumber()));
        }
        return ResponseEntity.ok(account);
    }

    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<?> delete(@PathVariable String accountNumber) {
        int rows = service.delete(accountNumber);
        if (rows == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Account not found", "accountNumber", accountNumber));
        }
        return ResponseEntity.ok(Map.of("message", "Account deleted successfully", "accountNumber", accountNumber));
    }

    /** Resolve account number from a registered card number. */
    @GetMapping("/by-card/{cardNumber}")
    public ResponseEntity<?> findByCard(@PathVariable String cardNumber) {
        String clean = cardNumber.replaceAll("[\\s\\-]", "");
        if (clean.length() < 13 || clean.length() > 19) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "INVALID_CARD", "message", "Card number must be 13-19 digits"));
        }
        String accountNumber = service.findAccountByCardNumber(clean);
        if (accountNumber == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "CARD_NOT_FOUND", "message", "No active account linked to this card number"));
        }
        Account account = service.findById(accountNumber);
        return ResponseEntity.ok(Map.of(
            "accountNumber", accountNumber,
            "currency", account != null && account.getCurrency() != null ? account.getCurrency() : ""
        ));
    }

    /** Resolve account number from a registered UPI ID. */
    @GetMapping("/by-upi")
    public ResponseEntity<?> findByUpi(@RequestParam(name = "id") String upiId) {
        if (upiId == null || upiId.isBlank() || !upiId.contains("@")) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "INVALID_UPI", "message", "UPI ID must be in the format name@provider"));
        }
        String accountNumber = service.findAccountByUpiId(upiId.trim());
        if (accountNumber == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "UPI_NOT_FOUND", "message", "No active account linked to UPI ID: " + upiId));
        }
        Account account = service.findById(accountNumber);
        return ResponseEntity.ok(Map.of(
            "accountNumber", accountNumber,
            "currency", account != null && account.getCurrency() != null ? account.getCurrency() : ""
        ));
    }

    /** Resolve account number from a registered mobile number. */
    @GetMapping("/by-mobile/{mobileNumber}")
    public ResponseEntity<?> findByMobile(@PathVariable String mobileNumber) {
        String clean = mobileNumber.replaceAll("[\\s\\-\\(\\)\\+]", "");
        if (clean.length() < 7 || clean.length() > 15) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "INVALID_MOBILE", "message", "Mobile number must be 7-15 digits"));
        }
        String accountNumber = service.findAccountByMobile(mobileNumber);
        if (accountNumber == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "MOBILE_NOT_FOUND", "message", "No active account linked to mobile: " + mobileNumber));
        }
        Account account = service.findById(accountNumber);
        return ResponseEntity.ok(Map.of(
            "accountNumber", accountNumber,
            "currency", account != null && account.getCurrency() != null ? account.getCurrency() : ""
        ));
    }

    /** Resolve account number from a registered wallet identifier. */
    @GetMapping("/by-wallet/{walletId}")
    public ResponseEntity<?> findByWallet(@PathVariable String walletId) {
        if (walletId == null || walletId.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "INVALID_WALLET", "message", "Wallet ID is required"));
        }
        String accountNumber = service.findAccountByWalletId(walletId);
        if (accountNumber == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "WALLET_NOT_FOUND", "message", "No active account linked to wallet: " + walletId));
        }
        Account account = service.findById(accountNumber);
        return ResponseEntity.ok(Map.of(
            "accountNumber", accountNumber,
            "currency", account != null && account.getCurrency() != null ? account.getCurrency() : ""
        ));
    }
}
