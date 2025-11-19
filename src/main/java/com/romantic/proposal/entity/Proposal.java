package com.romantic.proposal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "proposals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "unique_token", unique = true, nullable = false)
    private String uniqueToken;

    @Column(name = "shareable_link", nullable = false, length = 500)
    private String shareableLink;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private ProposalResponse response;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum ProposalResponse {
        YES, NO
    }
}