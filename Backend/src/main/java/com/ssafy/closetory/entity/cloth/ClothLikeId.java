package com.ssafy.closetory.entity.cloth;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ClothLikeId implements Serializable {

  @Column(name = "cloth_id")
  private Long clothId;

  @Column(name = "user_id")
  private Long userId;
}
