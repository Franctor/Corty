package com.corty.backend.services;

import com.corty.backend.dto.FriendResponse;
import com.corty.backend.exception.BusinessLogicException;
import com.corty.backend.exception.ResourceNotFoundException;
import com.corty.backend.model.Friendship;
import com.corty.backend.model.Player;
import com.corty.backend.model.User;
import com.corty.backend.model.enums.FriendshipStatus;
import com.corty.backend.model.enums.NotificationType;
import com.corty.backend.repository.FriendshipRepository;
import com.corty.backend.repository.PlayerRepository;
import com.corty.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final NotificationService notificationService;

    public List<FriendResponse> getFriends(String username) {
        User user = findUser(username);
        return friendshipRepository.findAllAcceptedFriends(user).stream()
                .map(f -> toResponse(f, user))
                .toList();
    }

    public List<FriendResponse> getPendingRequests(String username) {
        User user = findUser(username);
        List<Friendship> received = friendshipRepository
                .findByRecipientAndStatus(user, FriendshipStatus.PENDING);
        List<Friendship> sent = friendshipRepository
                .findByRequesterAndStatus(user, FriendshipStatus.PENDING);

        List<FriendResponse> result = new ArrayList<>();
        received.forEach(f -> result.add(toResponse(f, user)));
        sent.forEach(f -> result.add(toResponse(f, user)));
        return result;
    }

    @Transactional
    public FriendResponse sendRequest(String username, Long targetPlayerId) {
        User requester = findUser(username);
        Player targetPlayer = playerRepository.findById(targetPlayerId)
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado"));
        User recipient = targetPlayer.getUser();

        if (requester.getIdUser().equals(recipient.getIdUser())) {
            throw new BusinessLogicException("No puedes enviarte una solicitud a ti mismo");
        }

        Optional<Friendship> existing = friendshipRepository.findByRequesterAndRecipient(requester, recipient);
        Optional<Friendship> reverse  = friendshipRepository.findByRequesterAndRecipient(recipient, requester);

        if (existing.isPresent() || reverse.isPresent()) {
            throw new BusinessLogicException("Ya existe una relación con este jugador");
        }

        Friendship friendship = Friendship.builder()
                .requester(requester)
                .recipient(recipient)
                .status(FriendshipStatus.PENDING)
                .build();
        friendship = friendshipRepository.save(friendship);

        Player requesterPlayer = playerRepository.findByUser_IdUser(requester.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado"));

        notificationService.send(
                recipient.getIdUser(),
                NotificationType.FRIEND_REQUEST,
                "Nueva solicitud de amistad",
                requesterPlayer.getName() + " " + requesterPlayer.getSurname() + " quiere ser tu amigo",
                friendship.getIdFriendship()
        );

        return toResponse(friendship, requester);
    }

    @Transactional
    public FriendResponse acceptRequest(String username, Long friendshipId) {
        User user = findUser(username);
        Friendship friendship = findFriendship(friendshipId);

        if (!friendship.getRecipient().getIdUser().equals(user.getIdUser())) {
            throw new AccessDeniedException("No puedes aceptar esta solicitud");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new BusinessLogicException("La solicitud ya fue procesada");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);

        Player recipientPlayer = playerRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado"));

        notificationService.send(
                friendship.getRequester().getIdUser(),
                NotificationType.FRIEND_ACCEPTED,
                "Solicitud aceptada",
                recipientPlayer.getName() + " " + recipientPlayer.getSurname() + " ha aceptado tu solicitud",
                friendship.getIdFriendship()
        );

        return toResponse(friendship, user);
    }

    @Transactional
    public void declineOrRemove(String username, Long friendshipId) {
        User user = findUser(username);
        Friendship friendship = findFriendship(friendshipId);

        boolean isParticipant = friendship.getRequester().getIdUser().equals(user.getIdUser())
                || friendship.getRecipient().getIdUser().equals(user.getIdUser());
        if (!isParticipant) {
            throw new AccessDeniedException("No tienes acceso a esta amistad");
        }

        friendshipRepository.delete(friendship);
    }

    private FriendResponse toResponse(Friendship f, User viewer) {
        boolean iAmRequester = f.getRequester().getIdUser().equals(viewer.getIdUser());
        User otherUser = iAmRequester ? f.getRecipient() : f.getRequester();
        Player otherPlayer = playerRepository.findByUser_IdUser(otherUser.getIdUser()).orElse(null);

        return FriendResponse.builder()
                .friendshipId(f.getIdFriendship())
                .playerId(otherPlayer != null ? otherPlayer.getIdPlayer() : null)
                .userId(otherUser.getIdUser())
                .name(otherPlayer != null ? otherPlayer.getName() : otherUser.getUsername())
                .surname(otherPlayer != null ? otherPlayer.getSurname() : "")
                .avatarUrl(otherPlayer != null ? otherPlayer.getAvatarUrl() : null)
                .karma(otherPlayer != null ? otherPlayer.getKarma() : null)
                .status(f.getStatus().name())
                .iAmRequester(iAmRequester)
                .build();
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private Friendship findFriendship(Long id) {
        return friendshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Amistad no encontrada"));
    }
}
