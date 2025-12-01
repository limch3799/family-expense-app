package com.example.d105.domain.transaction.controller;

import com.example.d105.domain.transaction.dto.request.MakeTransactionRequest;
import com.example.d105.domain.transaction.service.MakeTransactionService;
import com.example.d105.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "4. 거래내역 임시 추가", description = "Transaction Management")
@Slf4j
public class MakeTransactionController {

    private final MakeTransactionService makeTransactionService;

    @PostMapping("/create/transactions")
    @Operation(
            summary = "트랜잭션 추가",
            description = "임의로 트랜잭션 추가"
    )
    public ResponseEntity<Void> createTransaction(@AuthenticationPrincipal CustomUserDetails user, @RequestBody List<MakeTransactionRequest.CreateTransactionRequest> requests ) {


        for(MakeTransactionRequest.CreateTransactionRequest request : requests){
            makeTransactionService.makeTransaction(user.getUser().getUserId(), request);
        }


        return ResponseEntity.ok().build();
    }

}
