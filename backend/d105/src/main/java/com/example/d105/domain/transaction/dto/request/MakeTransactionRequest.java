package com.example.d105.domain.transaction.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class MakeTransactionRequest {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateTransactionRequest{
        //cardId 혹은 accountId
        private Long transcationId;
        private Long id;
        private String merchantName;
        private Short categoryId;

        private String transactionUniqueNo;



        private LocalDate transactionDate;
        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        private LocalDateTime transactionDateTime;

        private String transactionType;
        private Integer amount;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionRequest{

     private   List<CreateTransactionRequest> transactionRequests;

    }
}
