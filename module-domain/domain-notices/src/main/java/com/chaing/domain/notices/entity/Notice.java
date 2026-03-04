package com.chaing.domain.notices.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.domain.notices.dto.command.NoticeCreateCommand;
import com.chaing.domain.notices.dto.command.NoticeUpdateCommand;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noticeId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private Long authorId;

    @Builder.Default
    @Column(nullable = false)
    private boolean important = false;

    public static Notice createNotice(NoticeCreateCommand command, Long authorId) {
        return Notice.builder()
                .title(command.title())
                .content(command.content())
                .authorId(authorId)
                .important(command.important())
                .build();
    }

    public void updateNotice(NoticeUpdateCommand command, Long authorId) {
        this.title = command.title();
        this.content = command.content();
        this.authorId = authorId;
        this.important = command.important();
    }
}
