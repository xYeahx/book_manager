package com.example.bms.mapper;

import com.example.bms.model.Transaction;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TransactionMapper {
    int insert(Transaction record);

    List<Transaction> selectByUserId(@Param("userid") Integer userid);
}
