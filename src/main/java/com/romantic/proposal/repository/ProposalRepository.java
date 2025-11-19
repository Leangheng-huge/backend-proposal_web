package com.romantic.proposal.repository;

import com.romantic.proposal.entity.Proposal;
import com.romantic.proposal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, UUID> {
    Optional<Proposal> findByUniqueToken(String uniqueToken);
    Optional<Proposal> findByIdAndUser(UUID id, User user);
}