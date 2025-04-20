package com.example.bankcards.controller;

import com.example.bankcards.dto.transaction.TransactionFilterDto;
import com.example.bankcards.dto.transaction.TransactionRequestDto;
import com.example.bankcards.dto.transaction.TransactionResponseDto;
import com.example.bankcards.entities.enums.TransactionType;
import com.example.bankcards.security.UserPrincipal;
import com.example.bankcards.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/{cardId}/withdraw")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Withdraw funds from a card",
            description = "Allows a user to withdraw funds from their card. Accessible only by users with the 'USER' role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully withdrawn funds"),
            @ApiResponse(responseCode = "400", description = "Bad request, invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden, you do not have permission to perform this action")
    })
    public ResponseEntity<Void> withdraw(@PathVariable UUID cardId,
                                         @RequestBody @Valid TransactionRequestDto dto,
                                         @AuthenticationPrincipal UserPrincipal user) {
        transactionService.withdraw(cardId, dto, user.getId());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/{fromCardId}/transfer/{toCardId}")
    @Operation(summary = "Transfer funds between cards",
            description = "Transfer funds from one card to another. Accessible by users with 'USER' or 'ADMIN' role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully transferred funds"),
            @ApiResponse(responseCode = "400", description = "Bad request, invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden, you do not have permission to perform this action")
    })
    public ResponseEntity<Void> transfer(@PathVariable UUID fromCardId,
                                         @PathVariable UUID toCardId,
                                         @RequestBody @Valid TransactionRequestDto dto,
                                         @AuthenticationPrincipal UserPrincipal user) {
        transactionService.transfer(fromCardId, toCardId, dto, user.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{cardId}/deposit")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Deposit funds into a card",
            description = "Allows a user to deposit funds into their card. Accessible only by users with the 'USER' role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deposited funds"),
            @ApiResponse(responseCode = "400", description = "Bad request, invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden, you do not have permission to perform this action")
    })
    public ResponseEntity<Void> deposit(@PathVariable UUID cardId,
                                        @RequestBody @Valid TransactionRequestDto dto,
                                        @AuthenticationPrincipal UserPrincipal user) {
        transactionService.deposit(cardId, dto, user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get transactions for a user",
            description = "Fetch a list of transactions for a user. Accessible by users with 'USER' or 'ADMIN' role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions")
    })
    public Page<TransactionResponseDto> getTransactions(
            @RequestParam(required = false) @Valid LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) TransactionType transactionType,
            @RequestParam(required = false) @Valid BigDecimal minAmount,
            @RequestParam(required = false) @Valid BigDecimal maxAmount,
            @RequestParam(required = false) UUID cardId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal user) {

        TransactionFilterDto filter = new TransactionFilterDto();
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setTransactionType(transactionType);
        filter.setMinAmount(minAmount);
        filter.setMaxAmount(maxAmount);
        filter.setCardId(cardId);

        PageRequest pageRequest = PageRequest.of(page, size);
        return transactionService.getAllTransactions(filter, pageRequest, user);
    }
}
