package com.ssafy.closetory.entity.clothes;

import com.ssafy.closetory.enums.ClothesColor;
import com.ssafy.closetory.enums.ClothesType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "clothes")
public class Clothes {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "photo_url", length = 255, nullable = false)
  private String photoUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "clothes_type", nullable = false)
  private ClothesType clothesType;

  @Enumerated(EnumType.STRING)
  @Column(name = "color", nullable = false)
  private ClothesColor color;

  @Column(name = "user_id", nullable = false)
  private Integer userId;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "clothes_seasons",
      joinColumns = @JoinColumn(name = "clothes_id"),
      inverseJoinColumns = @JoinColumn(name = "season_id"))
  @Builder.Default
  private Set<Season> seasons = new HashSet<>();

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "tags_clothes",
      joinColumns = @JoinColumn(name = "clothes_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id"))
  @Builder.Default
  private Set<Tag> tags = new HashSet<>();
}
