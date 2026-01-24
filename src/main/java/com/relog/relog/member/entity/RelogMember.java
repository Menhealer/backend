package com.relog.relog.member.entity;

import com.relog.relog.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "relog_member")
@SQLRestriction("is_deleted = false")
@SQLDelete(sql = "UPDATE relog_member SET is_deleted = true WHERE id = ?")
public class RelogMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", length = 100, unique = true, nullable = false)
    private String email;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "nickname", length = 20, nullable = false)
    private String nickname;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

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
