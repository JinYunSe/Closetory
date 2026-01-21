package com.ssafy.closetory.entity.clothes;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seasons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Season {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "season_name", length = 10, nullable = false)
  private String seasonName;
}
