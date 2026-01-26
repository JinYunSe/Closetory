package com.ssafy.closetory.entity.looks;

import com.ssafy.closetory.entity.clothes.Clothes;
import com.ssafy.closetory.entity.user.User;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "photo_url", nullable = false)
  private String photoUrl;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @ManyToMany
  @JoinTable(
      name = "clothes_looks",
      joinColumns = @JoinColumn(name = "look_id"),
      inverseJoinColumns = @JoinColumn(name = "clothes_id"))
  @Builder.Default
  private Set<Clothes> clothes = new HashSet<>();
}
