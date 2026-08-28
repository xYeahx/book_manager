package com.example.bms.service.impl;

import com.example.bms.mapper.TransactionMapper;
import com.example.bms.model.Transaction;
import com.example.bms.service.TransactionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Resource
    private TransactionMapper transactionMapper;

    @Override
    public int addTransaction(Transaction transaction) {
        return transactionMapper.insert(transaction);
    }

    @Override
    public List<Transaction> getTransactionsByUserId(Integer userid) {
        List<Transaction> list = transactionMapper.selectByUserId(userid);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Transaction t : list) {
            if (t.getCreateTime() != null) {
                t.setCreateTimeStr(sdf.format(t.getCreateTime()));
            }
        }
        return list;
    }
}
