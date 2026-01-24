# Re Log API 명세서

## 기본 정보
- **Base URL**: `http://localhost:8080`
- **인증 방식**: JWT Bearer Token
- **Content-Type**: `application/json`

---

## 공통 응답 형식

### 성공 응답
```json
{
  "success": true,
  "message": "Success",
  "data": { ... }
}
```

### 실패 응답
```json
{
  "success": false,
  "message": "에러 메시지",
  "data": null
}
```

---

## 1. 인증 (Auth)

### 1.1 회원가입
| 항목 | 내용 |
|------|------|
| URL | `POST /auth/signup` |
| 인증 | 불필요 |

**Request Body**
```json
{
  "email": "user@example.com",
  "password": "Password1!",
  "nickname": "닉네임",
  "birthday": "1990-01-01"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| email | String | O | 이메일 |
| password | String | O | 비밀번호 (영문+숫자+특수문자 8자 이상) |
| nickname | String | O | 닉네임 |
| birthday | String | X | 생년월일 (YYYY-MM-DD) |

**Response (201 Created)**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

---

### 1.2 로그인
| 항목 | 내용 |
|------|------|
| URL | `POST /auth/login` |
| 인증 | 불필요 |

**Request Body**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

---

### 1.3 이메일 중복확인
| 항목 | 내용 |
|------|------|
| URL | `POST /auth/email-check` |
| 인증 | 불필요 |

**Request Body**
```json
{
  "email": "user@example.com"
}
```

**Response (200 OK)**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "duplicate": true
  }
}
```

---

### 1.4 비밀번호 변경
| 항목 | 내용 |
|------|------|
| URL | `PUT /auth/password` |
| 인증 | 필요 |

**Request Body**
```json
{
  "currentPassword": "oldPassword1!",
  "newPassword": "newPassword1!"
}
```

---

## 2. 회원 (Members)

### 2.1 내 정보 조회
| 항목 | 내용 |
|------|------|
| URL | `GET /members/me` |
| 인증 | 필요 |

**Response (200 OK)**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "nickname": "닉네임",
    "birthday": "1990-01-01",
    "profileImage": "https://..."
  }
}
```

---

### 2.2 내 정보 수정
| 항목 | 내용 |
|------|------|
| URL | `PUT /members/me` |
| 인증 | 필요 |

**Request Body**
```json
{
  "nickname": "새닉네임",
  "birthday": "1990-05-15",
  "profileImage": "https://..."
}
```

---

### 2.3 회원 탈퇴
| 항목 | 내용 |
|------|------|
| URL | `DELETE /members/me` |
| 인증 | 필요 |

---

## 3. 친구 (Friends)

### 3.1 친구 등록
| 항목 | 내용 |
|------|------|
| URL | `POST /friends` |
| 인증 | 필요 |

**Request Body**
```json
{
  "name": "친구이름",
  "birthday": "1992-03-15",
  "groupId": 1
}
```

---

### 3.2 친구 목록 조회
| 항목 | 내용 |
|------|------|
| URL | `GET /friends` |
| 인증 | 필요 |
| Query | `groupId` (선택) |

---

### 3.3 친구 상세 조회
| 항목 | 내용 |
|------|------|
| URL | `GET /friends/{friendId}` |
| 인증 | 필요 |

**Response (200 OK)**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "friend": {
      "id": 1,
      "name": "친구이름",
      "birthday": "1992-03-15",
      "groupId": 1,
      "groupName": "동아리"
    },
    "relationshipScore": {
      "totalMeetings": 10,
      "averageScore": 4.2,
      "positiveCount": 7,
      "negativeCount": 1
    },
    "recentEvents": [...],
    "giftHistory": [...]
  }
}
```

---

### 3.4 친구 이름 중복확인
| 항목 | 내용 |
|------|------|
| URL | `POST /friends/name-check` |
| 인증 | 필요 |

**Request Body**
```json
{
  "name": "친구이름"
}
```

**Response (200 OK)**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "duplicate": true
  }
}
```

---

### 3.5 친구 수정
| 항목 | 내용 |
|------|------|
| URL | `PUT /friends/{friendId}` |
| 인증 | 필요 |

---

### 3.6 친구 삭제
| 항목 | 내용 |
|------|------|
| URL | `DELETE /friends/{friendId}` |
| 인증 | 필요 |

---

## 4. 단체 (Groups)

### 4.1 단체 생성
| 항목 | 내용 |
|------|------|
| URL | `POST /groups` |
| 인증 | 필요 |

**Request Body**
```json
{
  "name": "동아리"
}
```

---

### 4.2 단체 목록 조회
| 항목 | 내용 |
|------|------|
| URL | `GET /groups` |
| 인증 | 필요 |

---

### 4.3 단체 수정
| 항목 | 내용 |
|------|------|
| URL | `PUT /groups/{groupId}` |
| 인증 | 필요 |

---

### 4.4 단체 삭제
| 항목 | 내용 |
|------|------|
| URL | `DELETE /groups/{groupId}` |
| 인증 | 필요 |

---

## 5. 이벤트 (Events)

### 5.1 이벤트 등록
| 항목 | 내용 |
|------|------|
| URL | `POST /events` |
| 인증 | 필요 |

**Request Body**
```json
{
  "title": "동아리 정기 모임",
  "eventDate": "2025-01-20",
  "friendId": 1,
  "reviewScore": "GOOD",
  "reviewText": "좋았다"
}
```

---

### 5.2 날짜별 이벤트 조회
| 항목 | 내용 |
|------|------|
| URL | `GET /events?date={date}` |
| 인증 | 필요 |

---

### 5.3 이벤트 상세 조회
| 항목 | 내용 |
|------|------|
| URL | `GET /events/{eventId}` |
| 인증 | 필요 |

---

### 5.4 캘린더 월별 조회
| 항목 | 내용 |
|------|------|
| URL | `GET /events/calendar?year={year}&month={month}` |
| 인증 | 필요 |

---

### 5.5 이벤트 수정
| 항목 | 내용 |
|------|------|
| URL | `PUT /events/{eventId}` |
| 인증 | 필요 |

---

### 5.6 이벤트 삭제
| 항목 | 내용 |
|------|------|
| URL | `DELETE /events/{eventId}` |
| 인증 | 필요 |

---

## 6. 선물 (Gifts)

### 6.1 선물 기록 등록
| 항목 | 내용 |
|------|------|
| URL | `POST /gifts` |
| 인증 | 필요 |

**Request Body**
```json
{
  "itemName": "생일선물",
  "price": 30000,
  "giftDate": "2025-12-09",
  "giftType": "BIRTHDAY",
  "direction": "GIVEN",
  "description": "생일 케이크와 선물",
  "friendId": 1
}
```

---

### 6.2 선물 목록 조회
| 항목 | 내용 |
|------|------|
| URL | `GET /gifts` |
| 인증 | 필요 |
| Query | `friendId`, `type`, `direction` (모두 선택) |

**Response (200 OK)**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "itemName": "생일선물",
      "price": 30000,
      "giftDate": "2025-12-09",
      "giftType": "BIRTHDAY",
      "direction": "GIVEN",
      "description": "생일 케이크와 선물",
      "friendId": 1,
      "friendName": "햄스터"
    }
  ]
}
```

---

### 6.3 선물 수정
| 항목 | 내용 |
|------|------|
| URL | `PUT /gifts/{giftId}` |
| 인증 | 필요 |

---

### 6.4 선물 삭제
| 항목 | 내용 |
|------|------|
| URL | `DELETE /gifts/{giftId}` |
| 인증 | 필요 |

---

## 7. 정산 (Settlements)

### 7.1 월별 정산
| 항목 | 내용 |
|------|------|
| URL | `GET /settlements/monthly?year={year}&month={month}` |
| 인증 | 필요 |

---

### 7.2 분기별 정산
| 항목 | 내용 |
|------|------|
| URL | `GET /settlements/quarterly?year={year}&quarter={quarter}` |
| 인증 | 필요 |

---

## Enum 값

### ReviewScore
| 값 | 점수 |
|----|------|
| VERY_BAD | 1 |
| BAD | 2 |
| NEUTRAL | 3 |
| GOOD | 4 |
| VERY_GOOD | 5 |

### GiftType
| 값 | 설명 |
|----|------|
| BIRTHDAY | 생일 |
| WEDDING | 결혼 |
| FUNERAL | 장례 |
| CELEBRATION | 축하 |
| CONSOLATION | 위로 |
| OTHER | 기타 |

### GiftDirection
| 값 | 설명 |
|----|------|
| GIVEN | 준 것 |
| RECEIVED | 받은 것 |

---

## 에러 코드

| HTTP Status | 메시지 |
|-------------|--------|
| 400 | Validation failed |
| 400 | 비밀번호는 영문, 숫자, 특수문자를 포함하여 8자 이상이어야 합니다. |
| 400 | 현재 비밀번호가 올바르지 않습니다. |
| 401 | 이메일 또는 비밀번호가 올바르지 않습니다. |
| 404 | 회원을 찾을 수 없습니다. |
| 404 | 친구를 찾을 수 없습니다. |
| 404 | 단체를 찾을 수 없습니다. |
| 404 | 이벤트를 찾을 수 없습니다. |
| 404 | 선물 기록을 찾을 수 없습니다. |
| 409 | 이미 사용중인 이메일입니다. |
| 409 | 이미 존재하는 친구 이름입니다. |
| 409 | 이미 존재하는 단체 이름입니다. |
