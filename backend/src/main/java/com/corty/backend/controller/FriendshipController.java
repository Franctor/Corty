package com.corty.backend.controller;

import com.corty.backend.dto.FriendResponse;
import com.corty.backend.model.User;
import com.corty.backend.services.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Social")
@RestController
@RequestMapping("api/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    @GetMapping
    public ResponseEntity<List<FriendResponse>> getFriends(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(friendshipService.getFriends(currentUser.getUsername()));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<FriendResponse>> getPending(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(friendshipService.getPendingRequests(currentUser.getUsername()));
    }

    @PostMapping("/{playerId}/request")
    public ResponseEntity<FriendResponse> sendRequest(
            @PathVariable Long playerId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(201).body(friendshipService.sendRequest(currentUser.getUsername(), playerId));
    }

    @PostMapping("/{friendshipId}/accept")
    public ResponseEntity<FriendResponse> accept(
            @PathVariable Long friendshipId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(friendshipService.acceptRequest(currentUser.getUsername(), friendshipId));
    }

    @DeleteMapping("/{friendshipId}")
    public ResponseEntity<Void> declineOrRemove(
            @PathVariable Long friendshipId,
            @AuthenticationPrincipal User currentUser) {
        friendshipService.declineOrRemove(currentUser.getUsername(), friendshipId);
        return ResponseEntity.noContent().build();
    }
}
