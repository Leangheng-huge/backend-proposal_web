package com.romantic.proposal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusResponse {
    private UUID proposalId;
    private Boolean answered;
    private String response;
    private String notification;
    private LocalDateTime answeredAt;
}