# 오늘 얼마나 붐빌까? (boomvilmap)

전국 주요 관광지의 혼잡도를 한눈에 확인하는 Android 앱입니다. 현재 저장소는 백엔드나 AI 모델 서버 없이, Java 기반 Android 단일 앱 모듈로 구성되어 있습니다.

## 프로젝트 주제

**오늘 얼마나 붐빌까?**는 여행자가 관광지를 방문하기 전에 예상 혼잡도를 확인할 수 있도록 돕는 관광지 혼잡도 예측 앱입니다.

핵심 아이디어는 과거 관광객 데이터와 날짜 조건을 바탕으로 관광지별 방문객 수와 혼잡도를 제공하는 것입니다. 과거 날짜는 실제 데이터, 미래 날짜는 예측 데이터를 보여주는 방향으로 기획되어 있습니다.

## 서비스 내용

- 전국 주요 관광지를 지도와 목록에서 탐색합니다.
- 관광지별 현재 또는 예상 방문객 수를 확인합니다.
- 혼잡도를 `LOW`, `MODERATE`, `HIGH` 단계로 표시합니다.
- 관광지 상세 화면에서 지역, 카테고리, 설명, 방문객 수를 확인합니다.
- 자주 보는 관광지는 즐겨찾기 화면에서 관리하는 방향으로 설계합니다.
- 향후 공공데이터, 백엔드 API, AI 예측 모델을 연동해 실제 혼잡도 예측 서비스로 확장합니다.

## 기획 기능

| 기능 | 내용 | 현재 상태 |
| --- | --- | --- |
| 관광지 지도 | 전국 관광지 위치와 혼잡도를 지도에 표시 | 커스텀 지도 View와 샘플 핀 구현 |
| 관광지 목록 | 주요 관광지 목록 표시 | 샘플 데이터 기반 구현 |
| 관광지 상세 | 관광지 설명, 지역, 방문객 수, 혼잡도 표시 | 일부 구현 |
| 날짜별 혼잡도 | 과거는 실제 데이터, 미래는 예측 데이터 표시 | 기획 단계 |
| 주간 혼잡도 그래프 | 7일 단위 혼잡도 추이를 차트로 표시 | 커스텀 View 파일 존재, 연동 확장 필요 |
| 검색 | 지역/카테고리 기반 관광지 탐색 | 기본 목록 화면 구현 |
| 즐겨찾기 | 관심 관광지 저장 및 조회 | 샘플 목록 UI 구현 |
| 로그인/회원가입 | 사용자별 즐겨찾기와 개인화 기능 기반 | UI 및 화면 전환 구현 |
| AI 예측 | 방문객 수와 혼잡도 예측 | 서버/모델 미포함 |

## 현재 구현 범위

| 화면/기능 | 현재 상태 |
| --- | --- |
| 스플래시 | 앱 시작 화면 |
| 로그인/회원가입 | 화면 전환 및 비밀번호 표시 토글 중심의 UI 구현 |
| 지도 | `KoreaMapView` 커스텀 View로 대한민국 지도 형태와 관광지 핀 표시 |
| 관광지 목록 | 샘플 관광지 데이터를 `RecyclerView`로 표시 |
| 관광지 상세 | 선택한 관광지의 지역, 카테고리, 설명, 방문객 수 표시 |
| 검색 | 샘플 관광지 목록 표시 |
| 즐겨찾기 | 샘플 관광지 목록을 즐겨찾기 형태로 표시 |
| 마이페이지 | 기본 화면 UI |

현재 데이터는 `SpotRepository`에 하드코딩된 샘플 데이터입니다. 실제 API 연동, 로그인 인증, DB, Google Maps SDK, AI 예측 서버는 아직 프로젝트에 포함되어 있지 않습니다.

## 데이터 및 예측 확장 방향

향후 실제 서비스로 확장할 때 사용할 수 있는 데이터 후보입니다.

- 한국문화관광연구원 관광자원통계서비스
- 한국관광 데이터랩
- 공공데이터포털 주요관광지점 입장객통계
- 날씨, 공휴일, 주말, 계절, 지역 이벤트 정보

예측 결과는 다음과 같은 형태로 다룰 수 있습니다.

```json
{
  "data_type": "PREDICTED",
  "predicted_visitors": 14000,
  "congestion_level": "HIGH",
  "confidence": 0.85
}
```

혼잡도 기준 예시:

| 레벨 | 기준 예시 | 색상 |
| --- | --- | --- |
| LOW | 평균 대비 70% 이하 | Green |
| MODERATE | 평균 대비 70~130% | Yellow |
| HIGH | 평균 대비 130% 초과 | Red |

## 개발 환경

| 항목 | 값 |
| --- | --- |
| IDE | Android Studio |
| Language | Java |
| Gradle Wrapper | 9.3.1 |
| Android Gradle Plugin | 9.1.1 |
| Gradle/JVM Toolchain | JDK 21 |
| Java source/target compatibility | Java 11 |
| compileSdk | Android API 36, minor API 1 |
| minSdk | 35 |
| targetSdk | 36 |
| applicationId / namespace | `com.crowdcast.boomvilmap` |
| version | `1.0` (`versionCode` 1) |

## 주요 라이브러리

| 라이브러리 | 버전/용도 |
| --- | --- |
| AndroidX AppCompat | 1.7.1 |
| Material Components | 1.14.0 |
| AndroidX Activity | 1.13.0 |
| ConstraintLayout | 2.2.1 |
| RecyclerView | 1.4.0 |
| Glide | 5.0.7 |
| JUnit | 4.13.2 |
| AndroidX Test JUnit | 1.3.0 |
| Espresso | 3.7.0 |

의존성 버전은 `gradle/libs.versions.toml`과 `app/build.gradle`에 정의되어 있습니다.

## 프로젝트 구조

```text
boomvilmap/
├── app/
│   ├── build.gradle
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/crowdcast/boomvilmap/
│       │   │   ├── MainActivity.java
│       │   │   ├── SplashFragment.java
│       │   │   ├── LoginFragment.java
│       │   │   ├── SignUpFragment.java
│       │   │   ├── MapFragment.java
│       │   │   ├── SearchFragment.java
│       │   │   ├── FavoritesFragment.java
│       │   │   ├── MyPageFragment.java
│       │   │   ├── DetailFragment.java
│       │   │   ├── KoreaMapView.java
│       │   │   ├── WeeklyCongestionChartView.java
│       │   │   ├── Spot.java
│       │   │   ├── SpotRepository.java
│       │   │   ├── SpotAdapter.java
│       │   │   └── FavoriteAdapter.java
│       │   └── res/
│       │       ├── drawable/
│       │       ├── layout/
│       │       ├── menu/
│       │       ├── mipmap-*/
│       │       ├── values/
│       │       ├── values-night/
│       │       ├── values-sw390dp/
│       │       ├── values-sw600dp/
│       │       └── xml/
│       ├── androidTest/
│       └── test/
├── gradle/
│   ├── libs.versions.toml
│   ├── gradle-daemon-jvm.properties
│   └── wrapper/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── gradlew
└── gradlew.bat
```

## 화면 흐름

`MainActivity`가 단일 Activity로 동작하며 Fragment를 교체하는 구조입니다.

```text
SplashFragment
└── LoginFragment
    ├── SignUpFragment
    └── MapFragment
        ├── DetailFragment
        ├── SearchFragment
        ├── FavoritesFragment
        └── MyPageFragment
```

하단 내비게이션 탭은 지도, 검색, 즐겨찾기, 마이페이지 4개입니다.

## 로컬 설정

Android Studio에서 프로젝트를 열면 `local.properties`가 로컬 Android SDK 경로로 생성됩니다.

```properties
sdk.dir=C\:\\Users\\<사용자>\\AppData\\Local\\Android\\Sdk
```

`local.properties`, `.gradle/`, `.idea/`, `build/` 등 로컬/빌드 산출물은 `.gitignore`에 포함되어 있습니다.

## 실행 및 빌드

Windows PowerShell 기준:

```powershell
.\gradlew.bat assembleDebug
```

단위 테스트:

```powershell
.\gradlew.bat testDebugUnitTest
```

연결된 기기 또는 에뮬레이터에서 계측 테스트:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

Android Studio에서는 `app` 실행 구성을 선택한 뒤 에뮬레이터 또는 연결된 Android 기기에서 실행하면 됩니다.

## 테스트

현재 기본 템플릿 테스트가 포함되어 있습니다.

```text
app/src/test/java/com/crowdcast/boomvilmap/ExampleUnitTest.java
app/src/androidTest/java/com/crowdcast/boomvilmap/ExampleInstrumentedTest.java
```

## 향후 작업 후보

- 실제 검색 필터 및 즐겨찾기 저장 로직 추가
- API 클라이언트와 서버 연동 구조 추가
- 인증 세션 관리 구현
- 실제 지도 SDK 또는 지도 데이터 적용 여부 결정
- 혼잡도 예측 데이터 소스와 모델/API 설계
