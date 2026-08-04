package com.example.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.dto.CreateCardRequest;
import com.example.dto.CreateUserRequest;
import com.example.dto.CreateWalletRequest;
import com.example.dto.UpdateCardRequest;
import com.example.dto.UpdateUserRequest;
import com.example.dto.UpdateWalletRequest;
import com.example.model.User;
import com.example.model.UserCard;
import com.example.model.UserWallet;
import com.example.repository.UserRepository;

@Service
public class UserService {

    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("USD", "EUR", "GBP", "INR");
    private static final Set<String> SUPPORTED_WALLET_PROVIDERS = Set.of(
            "Google Pay", "PhonePe", "Paytm", "Amazon Pay", "Apple Pay");

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User createUser(CreateUserRequest request) {
        validateCreateRequest(request);

        if (repository.existsEmail(request.getEmail(), null)) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (repository.existsMobile(request.getMobileNumber(), null)) {
            throw new IllegalArgumentException("Mobile number already exists");
        }

        String accountNumber = repository.generateNextAccountNumber();
        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setAccountNumber(accountNumber);
        user.setFullName(request.getFullName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase(Locale.ROOT));
        user.setMobileNumber(request.getMobileNumber().trim());
        user.setOpeningBalance(request.getOpeningBalance());
        user.setPreferredCurrency(normalizeCurrency(request.getPreferredCurrency()));
        user.setNotes(normalizeOptionalText(request.getNotes()));
        user.setActive(true);
        user.setCreatedAt(now);

        repository.insertUser(user);

        String upiId = accountNumber.toLowerCase(Locale.ROOT) + "@payments";
        repository.insertUpi(accountNumber, upiId, "ACTIVE", now);

        User saved = repository.findUserByAccountNumber(accountNumber);
        if (saved != null) {
            return saved;
        }

        user.setUpiId(upiId);
        user.setUpiStatus("ACTIVE");
        return user;
    }

    public List<User> getUsers() {
        return repository.findAllUsers();
    }

    public User getUserByAccountNumber(String accountNumber) {
        User user = repository.findUserByAccountNumber(accountNumber);
        if (user == null) {
            throw new IllegalArgumentException("User not found for account: " + accountNumber);
        }
        return user;
    }

    public User updateUser(String accountNumber, UpdateUserRequest request) {
        User existing = getUserByAccountNumber(accountNumber);

        validateUpdateRequest(request);

        String nextEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);
        String nextMobile = request.getMobileNumber().trim();

        if (repository.existsEmail(nextEmail, accountNumber)) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (repository.existsMobile(nextMobile, accountNumber)) {
            throw new IllegalArgumentException("Mobile number already exists");
        }

        repository.updateUser(
                accountNumber,
                request.getFullName().trim(),
                nextEmail,
                nextMobile,
                normalizeCurrency(request.getPreferredCurrency()),
                normalizeOptionalText(request.getNotes()),
                request.getActive() == null ? existing.isActive() : request.getActive());

        return getUserByAccountNumber(accountNumber);
    }

    public User updateUserStatus(String accountNumber, boolean active) {
        getUserByAccountNumber(accountNumber);
        repository.updateUserStatus(accountNumber, active);
        return getUserByAccountNumber(accountNumber);
    }

    public UserCard addCard(String accountNumber, CreateCardRequest request) {
        getUserByAccountNumber(accountNumber);
        validateCardRequest(request);

        String cleanCardNumber = request.getCardNumber().trim();
        if (repository.existsCardNumber(cleanCardNumber)) {
            throw new IllegalArgumentException("Card number already exists");
        }

        repository.insertCard(
                accountNumber,
                cleanCardNumber,
                normalizeCardType(request.getCardType()),
                request.getCardBalance(),
                true);

        List<UserCard> cards = repository.findCardsByAccount(accountNumber);
        return cards.isEmpty() ? null : cards.get(0);
    }

    public List<UserCard> getCards(String accountNumber) {
        getUserByAccountNumber(accountNumber);
        return repository.findCardsByAccount(accountNumber);
    }

    public UserCard updateCard(String accountNumber, Long cardId, UpdateCardRequest request) {
        getUserByAccountNumber(accountNumber);
        validateUpdateCardRequest(request);

        UserCard existingCard = getCard(accountNumber, cardId);
        String cleanCardNumber = request.getCardNumber().trim();
        if (repository.existsCardNumber(cleanCardNumber, cardId)) {
            throw new IllegalArgumentException("Card number already exists");
        }

        repository.updateCard(cardId, cleanCardNumber, normalizeCardType(request.getCardType()), request.getCardBalance());
        return getCard(accountNumber, cardId);
    }

    public UserCard updateCardStatus(String accountNumber, Long cardId, boolean active) {
        getUserByAccountNumber(accountNumber);
        getCard(accountNumber, cardId);
        repository.updateCardStatus(cardId, active);
        return getCard(accountNumber, cardId);
    }

    public UserWallet addWallet(String accountNumber, CreateWalletRequest request) {
        getUserByAccountNumber(accountNumber);
        validateWalletRequest(request);

        String walletId = request.getWalletId().trim();
        if (repository.existsWalletId(walletId)) {
            throw new IllegalArgumentException("Wallet ID already exists");
        }

        repository.insertWallet(
                accountNumber,
                request.getWalletProvider().trim(),
                walletId,
                true);

        List<UserWallet> wallets = repository.findWalletsByAccount(accountNumber);
        return wallets.isEmpty() ? null : wallets.get(0);
    }

    public List<UserWallet> getWallets(String accountNumber) {
        getUserByAccountNumber(accountNumber);
        return repository.findWalletsByAccount(accountNumber);
    }

    public UserWallet updateWallet(String accountNumber, Long walletId, UpdateWalletRequest request) {
        getUserByAccountNumber(accountNumber);
        validateUpdateWalletRequest(request);

        UserWallet existingWallet = getWallet(accountNumber, walletId);
        String cleanedWalletId = request.getWalletId().trim();
        if (repository.existsWalletId(cleanedWalletId, walletId)) {
            throw new IllegalArgumentException("Wallet ID already exists");
        }

        repository.updateWallet(walletId, request.getWalletProvider().trim(), cleanedWalletId);
        return getWallet(accountNumber, walletId);
    }

    public UserWallet updateWalletStatus(String accountNumber, Long walletId, boolean active) {
        getUserByAccountNumber(accountNumber);
        getWallet(accountNumber, walletId);
        repository.updateWalletStatus(walletId, active);
        return getWallet(accountNumber, walletId);
    }

    private void validateCreateRequest(CreateUserRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (isBlank(request.getFullName())) {
            throw new IllegalArgumentException("Name required");
        }
        if (isBlank(request.getEmail())) {
            throw new IllegalArgumentException("Email required");
        }
        if (!isValidEmail(request.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (isBlank(request.getMobileNumber())) {
            throw new IllegalArgumentException("Mobile number required");
        }
        if (!request.getMobileNumber().trim().matches("^\\d{10}$")) {
            throw new IllegalArgumentException("Mobile number should contain 10 digits");
        }
        if (request.getOpeningBalance() == null || request.getOpeningBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Opening balance must be 0 or more");
        }
        if (request.getOpeningBalance().compareTo(new BigDecimal("9999999.99")) > 0) {
            throw new IllegalArgumentException("Opening balance cannot exceed 9,999,999.99");
        }
        if (isBlank(request.getPreferredCurrency())) {
            throw new IllegalArgumentException("Currency required");
        }
        normalizeCurrency(request.getPreferredCurrency());
    }

    private void validateUpdateRequest(UpdateUserRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (isBlank(request.getFullName())) {
            throw new IllegalArgumentException("Name required");
        }
        if (isBlank(request.getEmail())) {
            throw new IllegalArgumentException("Email required");
        }
        if (!isValidEmail(request.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (isBlank(request.getMobileNumber())) {
            throw new IllegalArgumentException("Mobile number required");
        }
        if (!request.getMobileNumber().trim().matches("^\\d{10}$")) {
            throw new IllegalArgumentException("Mobile number should contain 10 digits");
        }
        if (isBlank(request.getPreferredCurrency())) {
            throw new IllegalArgumentException("Currency required");
        }
        normalizeCurrency(request.getPreferredCurrency());
    }

    private void validateCardRequest(CreateCardRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Card request is required");
        }
        if (isBlank(request.getCardNumber())) {
            throw new IllegalArgumentException("Card number required");
        }
        if (!request.getCardNumber().trim().matches("^\\d{16}$")) {
            throw new IllegalArgumentException("Card number must contain 16 digits");
        }
        if (request.getCardBalance() == null || request.getCardBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Card balance must be 0 or more");
        }
        normalizeCardType(request.getCardType());
    }

    private void validateWalletRequest(CreateWalletRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Wallet request is required");
        }
        if (isBlank(request.getWalletProvider())) {
            throw new IllegalArgumentException("Wallet provider required");
        }
        if (!SUPPORTED_WALLET_PROVIDERS.contains(request.getWalletProvider().trim())) {
            throw new IllegalArgumentException("Unsupported wallet provider");
        }
        if (isBlank(request.getWalletId())) {
            throw new IllegalArgumentException("Wallet ID required");
        }
    }

    private void validateUpdateCardRequest(UpdateCardRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Card request is required");
        }
        if (isBlank(request.getCardNumber())) {
            throw new IllegalArgumentException("Card number required");
        }
        if (!request.getCardNumber().trim().matches("^\\d{16}$")) {
            throw new IllegalArgumentException("Card number must contain 16 digits");
        }
        if (request.getCardBalance() == null || request.getCardBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Card balance must be 0 or more");
        }
        normalizeCardType(request.getCardType());
    }

    private void validateUpdateWalletRequest(UpdateWalletRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Wallet request is required");
        }
        if (isBlank(request.getWalletProvider())) {
            throw new IllegalArgumentException("Wallet provider required");
        }
        if (!SUPPORTED_WALLET_PROVIDERS.contains(request.getWalletProvider().trim())) {
            throw new IllegalArgumentException("Unsupported wallet provider");
        }
        if (isBlank(request.getWalletId())) {
            throw new IllegalArgumentException("Wallet ID required");
        }
    }

    private UserCard getCard(String accountNumber, Long cardId) {
        UserCard card = repository.findCardById(accountNumber, cardId);
        if (card == null) {
            throw new IllegalArgumentException("Card not found");
        }
        return card;
    }

    private UserWallet getWallet(String accountNumber, Long walletId) {
        UserWallet wallet = repository.findWalletById(accountNumber, walletId);
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet not found");
        }
        return wallet;
    }

    private String normalizeCardType(String cardType) {
        if (isBlank(cardType)) {
            return "Debit";
        }
        String value = cardType.trim();
        // Accept both short form (Debit/Credit) and long form (Debit Card/Credit Card)
        if (value.toLowerCase().startsWith("debit")) return "Debit";
        if (value.toLowerCase().startsWith("credit")) return "Credit";
        throw new IllegalArgumentException("Card type must be Debit or Credit");
    }

    private String normalizeCurrency(String currency) {
        String normalized = currency.trim().toUpperCase(Locale.ROOT);
        if (!SUPPORTED_CURRENCIES.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported currency");
        }
        return normalized;
    }

    private String normalizeOptionalText(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isValidEmail(String email) {
        return email != null && email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }
}
