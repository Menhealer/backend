package com.relog.relog.member.entity;

import com.relog.relog.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "relog_member", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider", "provider_id"})
})
@SQLRestriction("is_deleted = false")
@SQLDelete(sql = "UPDATE relog_member SET is_deleted = true WHERE id = ?")
public class RelogMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private SocialProvider provider;

    @Column(name = "provider_id", nullable = false, length = 255)
    private String providerId;

    @Column(name = "nickname", length = 20, nullable = false)
    private String nickname;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
