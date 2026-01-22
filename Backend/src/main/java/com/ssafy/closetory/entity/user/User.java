package com.ssafy.closetory.entity.user;

import com.ssafy.closetory.enums.Gender;
import com.ssafy.closetory.enums.Provider;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "users",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_users_nickname", columnNames = "nickname"),
      @UniqueConstraint(name = "uk_users_username", columnNames = "username")
    })
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(length = 255)
  private String password;

  @Column(length = 30)
  private String nickname;

  @Enumerated(EnumType.STRING)
  private Gender gender;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Provider provider;

  private Short height;
  private Short weight;

  @Column(name = "provider_id", length = 128)
  private String providerId;

  @Column(length = 30)
  private String username; // 유저 아이디 (UK)

  @Column(name = "profile_photo_url", length = 255)
  private String profilePhotoUrl;

  @Column(name = "body_photo_url", length = 255)
  private String bodyPhotoUrl;

  @Column(name = "alarm_enabled", nullable = false)
  private Boolean alarmEnabled;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    if (this.alarmEnabled == null) {
      this.alarmEnabled = false;
    }
  }
}
