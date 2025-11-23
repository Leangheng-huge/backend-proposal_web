package com.romantic.proposal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusResponse {
    private boolean answered;
    private String response;
    private String notification;
    private String proposalId;
    private LocalDateTime answeredAt;
}