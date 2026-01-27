package com.ssafy.closetory.entity.post;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_item_clothes")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostItemClothes {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "post_id")
  private Integer postId;

  @Column(name = "clothes_id")
  private Integer clothesId;
}
