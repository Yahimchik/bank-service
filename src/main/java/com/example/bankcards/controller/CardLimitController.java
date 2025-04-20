package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardLimitRequestDto;
import com.example.bankcards.dto.card.CardLimitResponseDto;
import com.example.bankcards.security.UserPrincipal;
import com.example.bankcards.service.CardLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/card-limits")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class CardLimitController {

    private final CardLimitService cardLimitService;

    @GetMapping("/{cardId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get the limit for a specific card",
            description = "Fetch the limit details for a specific card, accessible by users with 'USER' or 'ADMIN' roles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched card limit")
    })
    public ResponseEntity<List<CardLimitResponseDto>> getCardLimit(
            @PathVariable UUID cardId,
            @AuthenticationPrincipal UserPrincipal user) {

        List<CardLimitResponseDto> cardLimitResponseDto = cardLimitService.getCardLimit(
                cardId, user.getId(), user.getRoles()
        );
        return ResponseEntity.ok(cardLimitResponseDto);
    }

    @PatchMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Set a new card limit",
            description = "Update the limit for a specific card, accessible only by users with the 'ADMIN' role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated card limit")
    })
    public ResponseEntity<CardLimitResponseDto> setCardLimit(
            @PathVariable UUID cardId,
            @RequestBody @Valid CardLimitRequestDto cardLimitRequestDto) {

        CardLimitResponseDto updatedCardLimit = cardLimitService.setCardLimit(cardId, cardLimitRequestDto);
        return ResponseEntity.ok(updatedCardLimit);
    }
}
