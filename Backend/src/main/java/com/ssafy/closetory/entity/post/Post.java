package com.ssafy.closetory.entity.post;

import com.ssafy.closetory.entity.clothes.Clothes;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, length = 30)
  private String title;

  @Column(name = "photo_url", nullable = false)
  private String photoUrl;

  @Column(columnDefinition = "TEXT")
  private String content;

  @Column(nullable = false)
  private Integer views;

  @Column(name = "user_id", nullable = false)
  private Integer userId;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Setter
  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @ManyToMany
  @JoinTable(
      name = "post_item_clothes",
      joinColumns = @JoinColumn(name = "post_id"),
      inverseJoinColumns = @JoinColumn(name = "clothes_id"))
  @Builder.Default
  @org.hibernate.annotations.Where(clause = "deleted_at IS NULL")
  private List<Clothes> clothes = new ArrayList<>();

  public void update(String title, String content) {
    this.title = title;
    this.content = content;
  }

  public void updatePhoto(String photoUrl) {
    this.photoUrl = photoUrl;
  }

  public void increaseViews() {
    this.views++;
  }

  public void delete() {
    this.deletedAt = LocalDateTime.now();
  }
}
