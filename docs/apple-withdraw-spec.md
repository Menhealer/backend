# Apple 회원 탈퇴 (Token Revocation) API 명세

## 엔드포인트

`DELETE /auth/withdraw`

## 요청

```json
{
  "provider": "APPLE",
  "identityToken": "eyJ...",
  "authorizationCode": "c1234..."
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| provider | String | O | "APPLE" 또는 "KAKAO" |
| identityToken | String | O (APPLE) | Apple id_token, 사용자 본인 확인용 |
| authorizationCode | String | O (APPLE) | Apple authorization code, 토큰 revoke용 |

## 백엔드 처리 순서

| 단계 | 할 일 | 설명 |
|------|--------|------|
| 1 | `identityToken` 검증 | 기존 `AppleAuthClient.getUserInfo()`로 본인 확인 |
| 2 | `authorizationCode` → token 교환 | Apple `/auth/token`에 POST → `refresh_token` 수령 |
| 3 | `refresh_token`으로 revoke | Apple `/auth/revoke`에 POST → Apple 측 연결 해제 |
| 4 | 우리 DB 회원 soft delete | BaseEntity의 `is_deleted = true` |
| 5 | Redis refresh token 삭제 | 로그아웃 처리 |

## Apple 서버 호출

### 1단계: Token 교환

```
POST https://appleid.apple.com/auth/token
Content-Type: application/x-www-form-urlencoded

client_id={Client ID}
&client_secret={Client Secret JWT}
&code={authorization_code}
&grant_type=authorization_code
```

**응답:**
```json
{
  "access_token": "...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "refresh_token": "...",
  "id_token": "..."
}
```

### 2단계: Token Revoke

```
POST https://appleid.apple.com/auth/revoke
Content-Type: application/x-www-form-urlencoded

client_id={Client ID}
&client_secret={Client Secret JWT}
&token={refresh_token}
&token_type_hint=refresh_token
```

**응답:** `200 OK` (body 없음)

## Client Secret (JWT) 생성

Apple의 `client_secret`은 일반 문자열이 아닌 JWT로, 매 요청마다 생성해야 한다.

**JWT Header:**
```json
{
  "alg": "ES256",
  "kid": "{Key ID}"
}
```

**JWT Payload:**
```json
{
  "iss": "{Team ID}",
  "iat": 1234567890,
  "exp": 1234567890,
  "aud": "https://appleid.apple.com",
  "sub": "{Client ID}"
}
```

**서명:** Apple Developer에서 발급받은 `.p8` Private Key (ES256)로 서명

## 필요한 설정값 (application.yml)

```yaml
apple:
  client-id: ${APPLE_CLIENT_ID}
  team-id: ${APPLE_TEAM_ID}
  key-id: ${APPLE_KEY_ID}
  private-key: ${APPLE_PRIVATE_KEY}   # .p8 파일 내용 (-----BEGIN PRIVATE KEY----- ... -----END PRIVATE KEY-----)
```

## 카카오 탈퇴 참고

같은 `DELETE /auth/withdraw` 엔드포인트를 사용하되, 카카오는 `authorizationCode` 불필요.

```json
{
  "provider": "KAKAO"
}
```

카카오는 카카오 API(`https://kapi.kakao.com/v1/user/unlink`)로 연결 끊기만 처리하면 된다.

## 응답

**성공:**
```json
{
  "success": true,
  "data": null
}
```

**실패 케이스:**
- 401: 인증 실패 (토큰 만료/유효하지 않음)
- 400: Apple revoke 실패
- 404: 존재하지 않는 회원
