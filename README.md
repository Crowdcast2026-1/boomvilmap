# 오늘 얼마나 붐빌까? (boomvilmap)

서울 주요 관광지의 현재 혼잡도와 미래 혼잡도 예측을 확인하는 Android 앱입니다.  
현재 앱은 Java 기반 Android 단일 모듈이며, Firebase, Google Maps SDK, FastAPI 백엔드, Open-Meteo 날씨 API를 연동합니다.

## 프로젝트 주제

**오늘 얼마나 붐빌까?**는 여행자가 관광지를 방문하기 전에 현재 혼잡도, 예상 인구, 날씨, 미래 날짜/시간대 혼잡도 예측을 확인할 수 있도록 돕는 관광지 혼잡도 앱입니다.

서울 주요 장소 POI 데이터를 기준으로 지도, 검색, 상세, 즐겨찾기, 마이페이지 기능을 제공합니다.

## 현재 구현 기능

| 기능 | 현재 상태 |
| --- | --- |
| 로그인/회원가입 | Firebase Auth 연동, Firestore 사용자 저장, 이미지 캡챠, 로그인 세션 유지 |
| 마이페이지 | 로그인 사용자 이메일/닉네임/가입일/즐겨찾기 수 표시, 닉네임 수정 |
| 지도 | Google Maps SDK 사용, 현재 위치 기반 주변 관광지 5개 표시, 전체보기 전환 |
| 지도 마커 | 혼잡도별 색상의 기본 핀 마커 표시, 마커 클릭 시 상세 화면 이동 |
| 지도 API 상태 | 서울 실시간 인구 API 로딩/에러/fallback 상태 표시, 새로고침 버튼 제공 |
| 지도 필터 | 혼잡도별 필터링 |
| 지도 검색 | 키워드가 포함된 관광지로 카메라 이동 |
| 검색 화면 | 한글 검색, 관광지명/지역/카테고리 검색, 혼잡도 높은순/낮은순 정렬 |
| 관광지 카드 | 사진, 지역, 카테고리, 혼잡도, 예상 인구, 데이터 출처 표시 |
| 관광지 상세 | 사진, 현재 혼잡도, 예상 인구, 기준 시간, 즐겨찾기, 날씨 표시 |
| 혼잡도 예측 | 미래 날짜/시간 선택 후 해당 시점 예측 혼잡도 표시 |
| 주간 예측 | 이번 주 또는 선택 날짜가 포함된 주의 혼잡도 예측 차트 표시 |
| 즐겨찾기 | 로그인 사용자별 Firestore 즐겨찾기 추가/삭제/조회 |

## 데이터 연동

### FastAPI 백엔드

Android 에뮬레이터 기준 기본 백엔드 주소는 다음과 같습니다.

```text
http://10.0.2.2:8000/
```

로컬 PC에서 직접 호출할 때의 백엔드 주소는 보통 다음과 같습니다.

```text
http://127.0.0.1:8000/
```

실제 Android 기기에서 실행하는 경우 `RetrofitClient`의 `BASE_URL`을 개발 PC의 같은 네트워크 IP로 변경해야 합니다.

현재 사용하는 주요 API:

| API | 용도 |
| --- | --- |
| `GET /population/current/all` | 서울 주요 장소 전체 현재 혼잡도/인구 조회 |
| `GET /population/current?area={areaName}` | 상세 화면 현재 혼잡도 조회 |
| `GET /predictions?area={areaName}&target_date=YYYY-MM-DD&target_time=HH:MM` | 미래 혼잡도 예측 |

`/population/current/all` 응답의 `areas` 배열을 기준으로 지도 마커, 주변 관광지 목록, 검색 목록을 렌더링합니다.

데이터 출처 표시는 다음 규칙을 따릅니다.

| `data_source` | 표시 |
| --- | --- |
| `live` | 실시간 |
| `database_fallback` | 최근 저장 데이터 |
| `unavailable` | 데이터 없음 |

`has_data`가 `false`이거나 데이터가 없는 장소는 혼잡도/인구 값을 `-`로 표시합니다.

### Firebase

Firebase는 다음 용도로 사용합니다.

- Firebase Auth: 이메일/비밀번호 로그인, 회원가입, 로그인 세션 확인
- Firestore `users`: 사용자 이메일, 닉네임, 가입일 저장
- Firestore 관광지 메타데이터: 장소명, POI 코드, 좌표, 지역, 카테고리, 이미지, 설명
- Firestore `users/{uid}/favorites`: 사용자별 즐겨찾기 저장

### Google Maps

지도 화면은 `SupportMapFragment` 기반 Google Maps SDK를 사용합니다.

- 서울 영역 기준 카메라 제한
- 현재 위치 권한 요청
- 핀치 줌, 줌 버튼, 스크롤/회전/기울이기 제스처 활성화
- 혼잡도별 기본 핀 마커 표시

### 날씨 API

상세 화면의 온도, 습도, 풍속, 날씨 상태는 Open-Meteo API를 사용합니다.

```text
https://api.open-meteo.com/v1/forecast
```

## 화면 구성

| 화면 | 설명 |
| --- | --- |
| Splash | 로그인 세션 확인 후 지도 또는 로그인 화면으로 이동 |
| Login | Firebase Auth 로그인 |
| SignUp | 캡챠 검증 후 Firebase Auth 회원가입 및 Firestore 사용자 저장 |
| Map | Google Maps, 주변 관광지, 혼잡도 필터, 검색, 새로고침 |
| Search | 관광지 검색 및 혼잡도 정렬 |
| Favorites | 사용자별 즐겨찾기 목록 |
| MyPage | 사용자 정보, 닉네임 수정, 로그아웃 |
| Detail | 관광지 상세, 즐겨찾기, 날씨, 날짜/시간별 예측, 주간 예측 |

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
| Firebase BoM | 33.1.1 |
| Firebase Auth | 로그인/회원가입 |
| Firebase Firestore | 사용자, 관광지 메타데이터, 즐겨찾기 |
| Google Maps SDK | 지도 |
| Google Play Services Location | 현재 위치 |
| Android Maps Utils | 지도 유틸 |
| Retrofit | 2.11.0 |
| Gson Converter | 2.11.0 |
| JUnit | 4.13.2 |
| AndroidX Test JUnit | 1.3.0 |
| Espresso | 3.7.0 |

## 프로젝트 구조

```text
boomvilmap/
├── app/
│   ├── build.gradle
│   ├── google-services.json        # 로컬 Firebase 설정 파일, gitignore 대상
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/crowdcast/boomvilmap/
│       │   │   ├── adepter/
│       │   │   │   ├── FavoriteAdapter.java
│       │   │   │   └── SpotAdapter.java
│       │   │   ├── model/
│       │   │   │   ├── CollectAllResponse.java
│       │   │   │   ├── CurrentPopulationResponse.java
│       │   │   │   ├── PredictionResponse.java
│       │   │   │   ├── Spot.java
│       │   │   │   ├── User.java
│       │   │   │   └── WeatherResponse.java
│       │   │   ├── network/
│       │   │   │   ├── RetrofitClient.java
│       │   │   │   └── SeoulCrowdApiService.java
│       │   │   ├── repository/
│       │   │   │   ├── AuthRepository.java
│       │   │   │   ├── FavoriteRepository.java
│       │   │   │   ├── SpotRepository.java
│       │   │   │   └── WeatherRepository.java
│       │   │   ├── ui/
│       │   │   │   ├── DetailFragment.java
│       │   │   │   ├── FavoritesFragment.java
│       │   │   │   ├── LoginFragment.java
│       │   │   │   ├── MainActivity.java
│       │   │   │   ├── MapFragment.java
│       │   │   │   ├── MyPageFragment.java
│       │   │   │   ├── SearchFragment.java
│       │   │   │   ├── SignUpFragment.java
│       │   │   │   ├── SplashFragment.java
│       │   │   │   └── WeeklyCongestionChartView.java
│       │   │   └── util/
│       │   │       ├── CaptchaGenerator.java
│       │   │       └── SeoulDataDumper.java
│       │   └── res/
│       ├── androidTest/
│       └── test/
├── gradle/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── gradlew
└── gradlew.bat
```

## 화면 흐름

`MainActivity`가 단일 Activity로 동작하며 Fragment를 교체합니다.

```text
SplashFragment
├── Firebase 로그인 세션 있음 -> MapFragment
└── Firebase 로그인 세션 없음 -> LoginFragment
    ├── SignUpFragment
    └── MapFragment
        ├── DetailFragment
        ├── SearchFragment
        ├── FavoritesFragment
        └── MyPageFragment
```

하단 내비게이션 탭은 지도, 검색, 즐겨찾기, 마이페이지 4개입니다. 상세 화면에서는 하단 내비게이션을 숨깁니다.

## 로컬 설정

Android Studio에서 프로젝트를 열면 `local.properties`가 로컬 Android SDK 경로로 생성됩니다. Google Maps API 키도 같은 파일에 설정합니다.

```properties
sdk.dir=C\:\\Users\\<사용자>\\AppData\\Local\\Android\\Sdk
MAPS_API_KEY=<Google Maps API Key>
```

Firebase를 사용하려면 Firebase 콘솔에서 Android 앱을 등록하고 `app/google-services.json` 파일을 추가해야 합니다. 이 파일은 `.gitignore`에 포함되어 있습니다.

FastAPI 백엔드는 앱 실행 전에 로컬에서 실행되어 있어야 합니다.

```text
http://127.0.0.1:8000
```

Android 에뮬레이터는 로컬 PC의 `127.0.0.1`에 직접 접근할 수 없으므로 앱 코드에서는 `http://10.0.2.2:8000/`을 사용합니다.

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

최근 확인한 명령:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
```

## 참고 및 남은 작업

- 실제 기기 테스트 시 `RetrofitClient.BASE_URL`을 개발 PC의 LAN IP로 변경해야 합니다.
- FastAPI 서버가 CORS를 제한하는 경우 백엔드에 허용 origin 설정이 필요합니다.
- Firestore 관광지 메타데이터와 FastAPI POI 코드가 일치해야 지도 좌표, 이미지, 카테고리가 안정적으로 표시됩니다.
- 카카오/Google 소셜 로그인 버튼은 UI가 있으나 현재 인증 로직은 이메일/비밀번호 기반입니다.
