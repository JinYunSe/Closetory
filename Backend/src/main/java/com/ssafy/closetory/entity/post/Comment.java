package com.ssafy.closetory.entity.post;

import jakarta.persistence.*;

@Entity
@Table(name = "comments")
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false)
  private String content;

  @Column(name = "post_id", nullable = false)
  private Integer postId;

  @Column(name = "user_id", nullable = false)
  private Integer userId;
}
