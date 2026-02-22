package com.project.InventoryMgtSys.services;

import com.project.InventoryMgtSys.dtos.Response;
import com.project.InventoryMgtSys.dtos.TransactionRequest;
import com.project.InventoryMgtSys.enums.TransactionStatus;

public interface TransactionService {
    Response purchase(TransactionRequest transactionRequest);

    Response sell(TransactionRequest transactionRequest);

    Response returnToSupplier(TransactionRequest transactionRequest);

    Response getAllTransactions(int page, int size, String filter);

    Response getAllTransactionById(Long id);

    Response getAllTransactionByMonthAndYear(int month, int year);

    Response updateTransactionStatus(Long transactionId, TransactionStatus status);
}
