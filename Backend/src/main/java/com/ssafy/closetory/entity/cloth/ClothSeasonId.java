package com.ssafy.closetory.entity.cloth;

import com.ssafy.closetory.enums.Season;
import jakarta.persistence.*;
import java.io.Serializable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ClothSeasonId implements Serializable {

  @Column(name = "cloth_id")
  private Long clothId;

  @Enumerated(EnumType.STRING)
  @Column(name = "season")
  private Season season;
}
