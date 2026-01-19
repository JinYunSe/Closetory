package com.ssafy.closetory.entity;

import com.ssafy.closetory.enums.ClothColor;
import com.ssafy.closetory.enums.ClothType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "clothes")
public class Cloth {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "cloth_id")
  private Long clothId;

  @Column(name = "cloth_image")
  private String clothImage;

  @Enumerated(EnumType.STRING)
  @Column(name = "cloth_type")
  private ClothType clothType;

  @Enumerated(EnumType.STRING)
  @Column(name = "color")
  private ClothColor color;

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner", nullable = false)
  private User owner;
}
