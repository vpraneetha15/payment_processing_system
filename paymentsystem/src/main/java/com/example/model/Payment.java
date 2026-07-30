package com.example.model;


import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name="payments")
public class Payment {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private double amount;


    private String currency;


    private String sourceAccount;


    private String destinationAccount;


    private String status;


    private LocalDateTime createdAt;



    public Long getId(){
        return id;
    }


    public double getAmount(){
        return amount;
    }


    public void setAmount(double amount){
        this.amount = amount;
    }


    public String getCurrency(){
        return currency;
    }


    public void setCurrency(String currency){
        this.currency = currency;
    }


    public String getSourceAccount(){
        return sourceAccount;
    }


    public void setSourceAccount(String sourceAccount){
        this.sourceAccount = sourceAccount;
    }


    public String getDestinationAccount(){
        return destinationAccount;
    }


    public void setDestinationAccount(String destinationAccount){
        this.destinationAccount = destinationAccount;
    }


    public String getStatus(){
        return status;
    }


    public void setStatus(String status){
        this.status=status;
    }


    public LocalDateTime getCreatedAt(){
        return createdAt;
    }


    public void setCreatedAt(LocalDateTime createdAt){
        this.createdAt=createdAt;
    }

}