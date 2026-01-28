package com.ssafy.closetory.entity.post;

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
public class PostItemClothesId implements Serializable {

  @Column(name = "post_id")
  private Integer postId;

  @Column(name = "clothes_id")
  private Integer clothesId;
}
