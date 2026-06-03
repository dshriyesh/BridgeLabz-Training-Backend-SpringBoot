package com.fundoonotes.entity;

import com.fundoonotes.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "in_app_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InAppNotification extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 512)
    private String message;

    @Column(nullable = false)
    private Boolean isRead;
}
