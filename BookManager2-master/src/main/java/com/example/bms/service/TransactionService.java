package com.example.bms.service;

import com.example.bms.model.Transaction;

import java.util.List;

public interface TransactionService {
    int addTransaction(Transaction transaction);
    List<Transaction> getTransactionsByUserId(Integer userid);
}
