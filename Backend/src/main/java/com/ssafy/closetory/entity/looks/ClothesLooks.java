package com.ssafy.closetory.entity.looks;

import com.ssafy.closetory.entity.clothes.Clothes;
import jakarta.persistence.*;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "clothes_looks")
public class ClothesLooks {
  @EmbeddedId private ClothesLooksId id;

  @MapsId("clothesId")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "clothes_id", nullable = false)
  private Clothes clothes;

  @MapsId("lookId")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "look_id", nullable = false)
  private Look look;
}
