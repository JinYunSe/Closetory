package com.ssafy.closetory.entity.cloth;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
public class ClothTag {

  @EmbeddedId private ClothTagId id;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}
