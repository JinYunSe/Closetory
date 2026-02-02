package com.ssafy.closetory.entity.user;

import com.ssafy.closetory.enums.Gender;
import com.ssafy.closetory.enums.Provider;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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
  @CreationTimestamp
  private LocalDateTime createdAt;

  @Setter
  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  public void changePassword(String encodedPassword) {
    this.password = encodedPassword;
  }

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    if (this.alarmEnabled == null) {
      this.alarmEnabled = false;
    }
  }

  public void updateNickname(String nickname) {
    this.nickname = nickname;
  }

  public void updateGender(Gender gender) {
    this.gender = gender;
  }

  public void updateHeight(Short height) {
    this.height = height;
  }

  public void updateWeight(Short weight) {
    this.weight = weight;
  }

  public void updateAlarmEnabled(Boolean alarmEnabled) {
    this.alarmEnabled = alarmEnabled;
  }

  public void updateProfilePhotoUrl(String profilePhotoUrl) {
    this.profilePhotoUrl = profilePhotoUrl;
  }

  public void updateBodyPhotoUrl(String bodyPhotoUrl) {
    this.bodyPhotoUrl = bodyPhotoUrl;
  }
}
