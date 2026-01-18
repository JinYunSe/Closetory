package com.ssafy.closetory.entity.cloth;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "cloth_saves")
@Getter
@Setter
@NoArgsConstructor
public class ClothSave {

  @EmbeddedId private ClothSaveId id;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}
