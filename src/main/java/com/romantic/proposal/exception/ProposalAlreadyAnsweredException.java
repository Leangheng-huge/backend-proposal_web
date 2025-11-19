package com.romantic.proposal.exception;

public class ProposalAlreadyAnsweredException extends RuntimeException {
    public ProposalAlreadyAnsweredException(String message) {
        super(message);
    }
}
