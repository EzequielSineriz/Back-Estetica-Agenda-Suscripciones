package com.AppEstetica.repository;

import com.AppEstetica.entities.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    @Query("""
        select t from Token t
        where t.user.id = :id and (t.isExpired = false or t.isRevoked = false)
        """)
    List<Token> findAllValidTokenByUser(Long id);

    Optional<Token> findByToken(String token);
}
