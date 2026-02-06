package com.ssafy.closetory.entity.clothes;

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
public class SaveId implements Serializable {

  @Column(name = "clothes_id")
  private Integer clothesId;

  @Column(name = "user_id")
  private Integer userId;
}
