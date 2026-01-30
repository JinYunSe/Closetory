package com.ssafy.closetory.entity.looks;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "looks")
public class Look {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "date")
  private LocalDate date;

  @Column(name = "reason", columnDefinition = "TEXT")
  private String reason;

  @JoinColumn(name = "user_id", nullable = false)
  private Integer userId;

  @Column(name = "photo_url", nullable = false)
  private String photoUrl;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
