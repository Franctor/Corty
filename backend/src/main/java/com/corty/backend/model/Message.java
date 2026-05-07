package com.corty.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "messages")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Message {
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_message")
    private Long idMessage;
    @Column(name = "sent_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime sentAt;
    @Builder.Default
    @Column(name = "is_read")
    private boolean read = false;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conversation", nullable = false)
    private Conversation conversation;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sender", nullable = true)
    private User sender;
}
