package com.ssafy.closetory.entity.user;

import com.ssafy.closetory.enums.Gender;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String password;

  @Column(unique = true)
  private String nickname;

  @Enumerated(EnumType.STRING)
  private Gender gender;

  private Integer height;
  private Integer weight;

  @Column(nullable = false)
  private String provider;

  @Column(name = "profile_image")
  private String profileImage;

  @Column(name = "full_body_photo")
  private String fullBodyPhoto;

  @Column(name = "user_id", unique = true)
  private String userId;

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime createdAt;
}
