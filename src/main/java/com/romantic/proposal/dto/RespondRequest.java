package com.romantic.proposal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespondRequest {
    @NotBlank(message = "Response is required")
    @Pattern(regexp = "YES|NO", message = "Response must be either YES or NO")
    private String response;
}