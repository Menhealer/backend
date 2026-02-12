package com.relog.relog.compatibility.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.relog.relog.compatibility.dto.CompatibilityResponse;
import com.relog.relog.compatibility.dto.CompatibilityResponse.CategoryResult;
import com.relog.relog.compatibility.dto.CompatibilityResponse.ElementBalance;
import com.relog.relog.compatibility.dto.CompatibilityResponse.ProfileInfo;
import com.relog.relog.compatibility.exception.BirthdayNotFoundException;
import com.relog.relog.friend.entity.Friend;
import com.relog.relog.friend.exception.FriendNotFoundException;
import com.relog.relog.friend.repository.FriendRepository;
import com.relog.relog.member.entity.RelogMember;
import com.relog.relog.member.exception.MemberNotFoundException;
import com.relog.relog.member.repository.RelogMemberRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompatibilityService {

    private final ChatClient.Builder chatClientBuilder;
    private final RelogMemberRepository memberRepository;
    private final FriendRepository friendRepository;
    private final ObjectMapper objectMapper;

    private static final String[] ZODIAC_ANIMALS = {
            "원숭이", "닭", "개", "돼지", "쥐", "소",
            "호랑이", "토끼", "용", "뱀", "말", "양"
    };

    public CompatibilityResponse analyze(Long memberId, Long friendId) {
        RelogMember member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        if (member.getBirthday() == null) {
            throw new BirthdayNotFoundException("생년월일 정보가 등록되지 않았습니다. 프로필에서 생년월일을 등록해주세요.");
        }

        Friend friend = friendRepository.findByIdAndMemberId(friendId, memberId)
                .orElseThrow(FriendNotFoundException::new);

        if (friend.getBirthday() == null) {
            throw new BirthdayNotFoundException("친구의 생년월일 정보가 등록되지 않았습니다.");
        }

        String prompt = buildPrompt(
                member.getNickname(), member.getBirthday(), member.getBirthTime(),
                friend.getName(), friend.getBirthday(),
                friend.getGroup()
        );

        ChatClient chatClient = chatClientBuilder.build();
        String aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return parseResponse(aiResponse, member, friend);
    }

    private String buildPrompt(String memberName, LocalDate memberBirthday, LocalTime memberBirthTime,
                               String friendName, LocalDate friendBirthday,
                               String friendGroup) {
        int currentYear = Year.now().getValue();
        String currentYearAnimal = ZODIAC_ANIMALS[currentYear % 12];
        String relationshipContext = buildRelationshipContext(friendGroup);
        String birthTimeContext = buildBirthTimeContext(memberName, memberBirthTime);

        return String.format("""
                너는 10~30대가 좋아하는 친근하고 재밌는 사주/궁합 전문가야!
                만세력 기반으로 두 사람의 사주팔자를 분석하고, 궁합을 봐줘.

                분석할 때 다음을 모두 고려해줘:
                - 사주팔자: 연주(년간/년지), 월주(월간/월지), 일주(일간/일지) 계산
                - 오행(목/화/토/금/수) 밸런스와 상생/상극 관계
                - 십신 관계 (비견, 식신, 정재, 편관 등)
                - 합(삼합, 육합), 충, 형, 파 관계
                - 띠(12지신)와 별자리 궁합
                - 올해 세운(연운) 영향

                말투는 ~해요, ~이에요 같은 부드러운 해요체로 해줘.
                딱딱하지 않게, 친구한테 얘기하듯이 쉽고 재밌게 풀어서 설명해줘.
                사주 용어는 괄호 안에 쉬운 설명을 넣어줘! (예: "일간이 갑목(나무 기운)이에요")
                이모지도 적절히 써줘!

                - 사람 1: %s, 생년월일: %s
                - 사람 2: %s, 생년월일: %s
                - 두 사람의 관계: %s
                - 현재 연도: %d년 (%s의 해)

                %s
                %s

                중요: 사람 2의 태어난 시간은 알 수 없어요.
                시간에 따라 달라질 수 있는 부분(시주, 일부 오행)은 "태어난 시간에 따라 조금 달라질 수 있어요~" 라고 자연스럽게 언급해줘.
                확실한 정보(띠, 별자리, 연주/월주)는 확신 있게, 불확실한 부분은 솔직하게 범위로 알려줘!

                반드시 아래 JSON 형식으로만 응답해줘. JSON 외의 텍스트는 쓰지 마:
                {
                  "summary": "두 사람의 궁합을 관계 유형에 맞게 한 줄로 재밌게 요약",
                  "overallScore": (1~100 정수),
                  "memberProfile": {
                    "name": "%s",
                    "zodiacAnimal": "띠 동물 (예: 호랑이)",
                    "zodiacSign": "별자리 (예: 물고기자리)",
                    "element": "일간 기준 오행 (예: 갑목(나무))",
                    "sajuPillar": "연주·월주·일주 요약 (예: 갑자년 을축월 병인일)",
                    "personality": "사주 기반 성격 요약 2~3문장 (쉽게!)"
                  },
                  "friendProfile": {
                    "name": "%s",
                    "zodiacAnimal": "띠 동물",
                    "zodiacSign": "별자리",
                    "element": "일간 기준 오행 (시간 모르면 날짜 기준 추정)",
                    "sajuPillar": "연주·월주·일주 요약",
                    "personality": "사주 기반 성격 요약 2~3문장 (쉽게!)"
                  },
                  "analysis": "전체 궁합 분석 - 두 사람의 오행 조화, 합/충 관계, 상생/상극을 쉽게 풀어서 5~7문장",
                  "elementBalance": {
                    "description": "두 사람의 오행 조합이 어떤지 쉽게 설명 (2~3문장)",
                    "strongElement": "두 사람이 함께할 때 강해지는 기운 (예: 목(나무) 기운)",
                    "weakElement": "부족한 기운 (예: 수(물) 기운이 약해요)",
                    "interaction": "합/충/형 관계 쉽게 설명 (예: 삼합이 있어서 자연스럽게 잘 맞아요!)"
                  },
                  "categories": [
                    {"category": "성격 궁합", "emoji": "적절한 이모지 1개", "score": (1~100), "description": "십신 관계 기반으로 쉽고 재밌게 3~4문장"},
                    {"category": "관계 유형에 맞는 궁합 (예: 우정/연인/업무 궁합)", "emoji": "적절한 이모지 1개", "score": (1~100), "description": "쉽고 재밌게 3~4문장"},
                    {"category": "대화 궁합", "emoji": "적절한 이모지 1개", "score": (1~100), "description": "쉽고 재밌게 3~4문장"},
                    {"category": "%d년(%s의 해) 올해의 궁합", "emoji": "적절한 이모지 1개", "score": (1~100), "description": "올해 세운 기준으로 쉽고 재밌게 3~4문장"}
                  ],
                  "advice": "이 관계를 더 좋게 만들 구체적인 팁 (오행 보완 팁 포함, 관계 유형에 맞게 3~4문장)",
                  "luckyItem": "두 사람의 부족한 오행을 채워줄 행운 아이템이나 활동 (예: '물 기운이 부족하니까 같이 바다 여행 가보세요!')"
                }
                """,
                memberName, memberBirthday,
                friendName, friendBirthday,
                friendGroup != null ? friendGroup : "친구",
                currentYear, currentYearAnimal,
                relationshipContext,
                birthTimeContext,
                memberName,
                friendName,
                currentYear, currentYearAnimal
        );
    }

    private String buildBirthTimeContext(String memberName, LocalTime birthTime) {
        if (birthTime == null) {
            return String.format("참고: %s의 태어난 시간도 알 수 없어서, 두 사람 모두 양력 생년월일만으로 분석해줘.", memberName);
        }
        return String.format("참고: %s의 태어난 시간은 %s이에요. 이 정보로 시주까지 포함해서 더 정확하게 분석해줘!", memberName, birthTime);
    }

    private String buildRelationshipContext(String group) {
        if (group == null || group.isBlank()) {
            return "일반적인 친구 관계로 분석해줘.";
        }
        return String.format("""
                두 사람은 '%s' 관계야. 이 관계 유형에 맞춰서 분석해줘!
                예를 들어:
                - 연인/썸이면 → 연애 궁합, 데이트 팁 중심으로
                - 직장동료/비즈니스면 → 업무 시너지, 협업 궁합 중심으로
                - 친구/베프면 → 우정 궁합, 같이 놀 때 케미 중심으로
                - 가족이면 → 가족 간 조화, 소통 궁합 중심으로
                이런 식으로 관계에 딱 맞는 관점에서 분석해줘!""", group);
    }

    private CompatibilityResponse parseResponse(String aiResponse, RelogMember member, Friend friend) {
        try {
            String json = extractJson(aiResponse);
            AiCompatibilityResult result = objectMapper.readValue(json, AiCompatibilityResult.class);

            return CompatibilityResponse.builder()
                    .memberName(member.getNickname())
                    .friendName(friend.getName())
                    .memberBirthday(member.getBirthday())
                    .friendBirthday(friend.getBirthday())
                    .summary(result.summary)
                    .overallScore(result.overallScore)
                    .memberProfile(toProfileInfo(result.memberProfile))
                    .friendProfile(toProfileInfo(result.friendProfile))
                    .analysis(result.analysis)
                    .elementBalance(toElementBalance(result.elementBalance))
                    .categories(result.categories.stream()
                            .map(c -> CategoryResult.builder()
                                    .category(c.category)
                                    .emoji(c.emoji)
                                    .score(c.score)
                                    .description(c.description)
                                    .build())
                            .toList())
                    .advice(result.advice)
                    .luckyItem(result.luckyItem)
                    .build();
        } catch (Exception e) {
            log.warn("[Compatibility] AI 응답 파싱 실패, 기본 응답 반환: {}", e.getMessage());
            return buildFallbackResponse(member, friend);
        }
    }

    private ProfileInfo toProfileInfo(AiProfileResult profile) {
        if (profile == null) {
            return null;
        }
        return ProfileInfo.builder()
                .name(profile.name)
                .zodiacAnimal(profile.zodiacAnimal)
                .zodiacSign(profile.zodiacSign)
                .element(profile.element)
                .sajuPillar(profile.sajuPillar)
                .personality(profile.personality)
                .build();
    }

    private ElementBalance toElementBalance(AiElementBalanceResult balance) {
        if (balance == null) {
            return null;
        }
        return ElementBalance.builder()
                .description(balance.description)
                .strongElement(balance.strongElement)
                .weakElement(balance.weakElement)
                .interaction(balance.interaction)
                .build();
    }

    private String extractJson(String response) {
        String trimmed = response.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }

    private CompatibilityResponse buildFallbackResponse(RelogMember member, Friend friend) {
        return CompatibilityResponse.builder()
                .memberName(member.getNickname())
                .friendName(friend.getName())
                .memberBirthday(member.getBirthday())
                .friendBirthday(friend.getBirthday())
                .summary("잠시 후 다시 확인해주세요!")
                .overallScore(75)
                .analysis("사주 분석 서비스에 잠깐 문제가 생겼어요. 조금만 기다려주세요!")
                .categories(List.of(
                        CategoryResult.builder().category("성격 궁합").emoji("😊").score(75).description("잠시 후 다시 확인해주세요!").build(),
                        CategoryResult.builder().category("우정 궁합").emoji("🤝").score(75).description("잠시 후 다시 확인해주세요!").build(),
                        CategoryResult.builder().category("대화 궁합").emoji("💬").score(75).description("잠시 후 다시 확인해주세요!").build(),
                        CategoryResult.builder().category("올해의 궁합").emoji("✨").score(75).description("잠시 후 다시 확인해주세요!").build()
                ))
                .advice("잠시 후 다시 시도해주세요!")
                .luckyItem("조금만 기다려주세요!")
                .build();
    }

    private static class AiCompatibilityResult {
        public String summary;
        public int overallScore;
        public AiProfileResult memberProfile;
        public AiProfileResult friendProfile;
        public String analysis;
        public AiElementBalanceResult elementBalance;
        public List<AiCategoryResult> categories;
        public String advice;
        public String luckyItem;
    }

    private static class AiProfileResult {
        public String name;
        public String zodiacAnimal;
        public String zodiacSign;
        public String element;
        public String sajuPillar;
        public String personality;
    }

    private static class AiElementBalanceResult {
        public String description;
        public String strongElement;
        public String weakElement;
        public String interaction;
    }

    private static class AiCategoryResult {
        public String category;
        public String emoji;
        public int score;
        public String description;
    }
}
