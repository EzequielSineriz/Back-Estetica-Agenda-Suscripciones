package com.AppEstetica.entities;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public final class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "token_type", length = 50)
    private TokenType tokenType = TokenType.BEARER;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRevoked = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isExpired = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public enum TokenType {
        REFRESH, BEARER
    }
}
