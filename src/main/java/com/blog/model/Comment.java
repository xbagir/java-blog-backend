package com.blog.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("comments")
public class Comment {

    @Id
    @Column("id")
    private Long id;

    @Column("post_id")
    private Long postId;

    @Column("text")
    private String text;

    @Column("created_at")
    @ReadOnlyProperty
    private Instant createdAt;
}
