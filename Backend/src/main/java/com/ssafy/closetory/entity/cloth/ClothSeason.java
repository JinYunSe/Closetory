package com.ssafy.closetory.entity.cloth;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "seasons")
@Getter
@Setter
@NoArgsConstructor
public class ClothSeason {

  @EmbeddedId private ClothSeasonId id;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}
