package com.relog.relog.event.entity;

import com.relog.relog.common.BaseEntity;
import com.relog.relog.friend.entity.Friend;
import com.relog.relog.member.entity.RelogMember;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@Table(name = "relog_event")
@SQLRestriction("is_deleted = false")
@SQLDelete(sql = "UPDATE relog_event SET is_deleted = true WHERE id = ?")
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", length = 12, nullable = false)
    private String title;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_score")
    private ReviewScore reviewScore;

    @Column(name = "review_text", length = 100)
    private String reviewText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private RelogMember member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false)
    private Friend friend;

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public void updateReview(ReviewScore reviewScore, String reviewText) {
        this.reviewScore = reviewScore;
        this.reviewText = reviewText;
    }

    public void updateFriend(Friend friend) {
        this.friend = friend;
    }
}
