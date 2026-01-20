package com.ssafy.closetory.entity.cloth;

import jakarta.persistence.*;
import java.io.Serializable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ClothTagId implements Serializable {

  @Column(name = "cloth_id")
  private Long clothId;

  @Column(name = "tag")
  private String tag;
}
