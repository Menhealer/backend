package com.relog.relog.gift.entity;

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
@Table(name = "relog_gift")
@SQLRestriction("is_deleted = false")
@SQLDelete(sql = "UPDATE relog_gift SET is_deleted = true WHERE id = ?")
public class Gift extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_name", length = 100, nullable = false)
    private String itemName;

    @Column(name = "price")
    private Integer price;

    @Column(name = "gift_date", nullable = false)
    private LocalDate giftDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "gift_type", nullable = false)
    private GiftType giftType;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false)
    private GiftDirection direction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private RelogMember member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false)
    private Friend friend;

    public void updateItemName(String itemName) {
        this.itemName = itemName;
    }

    public void updatePrice(Integer price) {
        this.price = price;
    }

    public void updateGiftDate(LocalDate giftDate) {
        this.giftDate = giftDate;
    }

    public void updateGiftType(GiftType giftType) {
        this.giftType = giftType;
    }

    public void updateDirection(GiftDirection direction) {
        this.direction = direction;
    }
}
