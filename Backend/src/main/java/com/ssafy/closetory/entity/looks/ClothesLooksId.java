package com.ssafy.closetory.entity.looks;

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
public class ClothesLooksId implements Serializable {
  @Column(name = "clothes_id")
  private Integer clothesId;

  @Column(name = "look_id")
  private Integer lookId;
}
