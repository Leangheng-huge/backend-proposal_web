package com.romantic.proposal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposalResponse {
    private String proposalId;
    private String uniqueToken;
    private String shareableLink;
    private String createdAt;
    private String message;
}