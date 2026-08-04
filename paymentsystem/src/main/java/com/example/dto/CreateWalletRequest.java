package com.example.dto;

public class CreateWalletRequest {

    private String walletProvider;
    private String walletId;

    public CreateWalletRequest() {
    }

    public String getWalletProvider() {
        return walletProvider;
    }

    public void setWalletProvider(String walletProvider) {
        this.walletProvider = walletProvider;
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }
}
