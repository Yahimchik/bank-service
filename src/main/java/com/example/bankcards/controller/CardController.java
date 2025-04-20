package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardFilterDto;
import com.example.bankcards.dto.card.CardRequestDto;
import com.example.bankcards.dto.card.CardResponseDto;
import com.example.bankcards.entities.enums.CardStatus;
import com.example.bankcards.security.UserPrincipal;
import com.example.bankcards.service.CardService;
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
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class CardController {

    private final CardService cardService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new card", description = "Only ADMIN role can create a new card for a user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created a new card")
    })
    public ResponseEntity<CardResponseDto> createCard(@RequestBody @Valid CardRequestDto request,
                                                      @RequestParam UUID userId) {
        return ResponseEntity.ok(cardService.createCard(request, userId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get a list of all cards", description = "Admin can retrieve a list of all cards with optional filters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of cards")
    })
    public Page<CardResponseDto> getCards(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) BigDecimal minBalance,
            @RequestParam(required = false) BigDecimal maxBalance,
            @RequestParam(required = false) Boolean isDeleted,
            @RequestParam(required = false) Boolean requestForBlocking,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        CardFilterDto filter = new CardFilterDto();
        filter.setUserId(userId);
        filter.setStatus(status);
        filter.setMinBalance(minBalance);
        filter.setMaxBalance(maxBalance);
        filter.setIsDeleted(isDeleted);
        filter.setRequestedForBlocking(requestForBlocking);

        PageRequest pageRequest = PageRequest.of(page, size);

        return cardService.getAllCards(filter, pageRequest);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get all cards of the current user", description = "Fetch the list of cards associated with the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of user cards")
    })
    public ResponseEntity<List<CardResponseDto>> getUserCards(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(cardService.getUserCards(user.getId()));
    }

    @PatchMapping("/{cardId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Block a card", description = "Block a card, accessible by users with 'ADMIN' role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card successfully blocked"),
            @ApiResponse(responseCode = "404", description = "Card not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required")
    })
    public ResponseEntity<Void> blockCard(@PathVariable UUID cardId) {
        cardService.blockCard(cardId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{cardId}/request-block")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Request card blocking", description = "Users can request blocking of their card. Requires 'USER' role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card blocking request successfully submitted"),
            @ApiResponse(responseCode = "404", description = "Card not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required")
    })
    public ResponseEntity<Void> requestCardBlocking(@PathVariable UUID cardId,
                                                    @AuthenticationPrincipal UserPrincipal userPrincipal) {
        cardService.requestCardBlocking(cardId, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{cardId}/reject-block-request")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject card block request", description = "Admin can reject a user's card blocking request.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card block request successfully rejected"),
            @ApiResponse(responseCode = "404", description = "Card not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required")
    })
    public ResponseEntity<Void> rejectCardBlockRequest(@PathVariable UUID cardId,
                                                       @AuthenticationPrincipal UserPrincipal adminUser) {
        cardService.rejectCardBlockRequest(cardId, adminUser);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{cardId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate a card", description = "Admin can activate a card after it was blocked.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card successfully activated"),
            @ApiResponse(responseCode = "404", description = "Card not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required")
    })
    public ResponseEntity<Void> activateCard(@PathVariable UUID cardId) {
        cardService.activateCard(cardId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a card", description = "Delete a card, accessible only by users with 'ADMIN' role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Card successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Card not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required")
    })
    public ResponseEntity<Void> deleteCard(@PathVariable UUID cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }
}
