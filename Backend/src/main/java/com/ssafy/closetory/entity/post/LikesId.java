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
public class LikesId implements Serializable {
  @Column(name = "user_id")
  private Integer userId;

  @Column(name = "post_id")
  private Integer postId;
}
