# 코딩랩 모드팩 시즌 2
이 리포지토리는 코딩랩 공식 마인크래프트 1.21.1 모드팩 서버의 컨텐츠들을 제공하는 모드들의 소스를 담고 있는 모노레포 입니다.

## 빌드 방법

이 레포의 모드들을 빌드하기 위해선 아래 준비물들이 필요합니다:

1. Java 25 (Gradle 데몬 실행용)
2. Java 21 (마인크래프트 실행용)
3. Rust Toolchain (josh 설치 및 기타 스크립트 실행용)
4. 16GB+ 메모리

```sh
cargo install josh-cli --locked --git https://github.com/josh-project/josh.git # josh 설치
josh clone https://github.com/coding-1ab/modpack-season-2.git :/ ./modpack-season2 # 리포지토리 내려받기
cd modpack-season-2
rustc add_remotes.rs # 스크립트 컴파일
rustc fetch.rs # 스크립트 컴파일
./add_remotes # 업스트림 리모트 추가
./fetch # 업스트림에서 커밋 데이터 가져오기
```

Gradle 데몬에는 Java 25가 필요합니다. Gradle 데몬을 실행하기 전 먼저 `JAVA_HOME`을 JDK 25 설치 경로로 지정해야 합니다.

### 클라이언트 실행
변경 사항을 만든 이후 클라이언트를 실행하려면 아래 명령을 사용합니다:

```sh
# 로그인 없이 실행
./gradlew runClient

# 로그인 후 실행
./gradlew runClientAuth
```

병렬 실행이 기본으로 활성화되어 있습니다. 메모리가 부족하면 `gradle.properties`에서 병렬 실행을 임시로 끌 수 있습니다.

## 개별 모드 작업

Josh CLI를 사용하면 모드 하나의 파일과 이력만 보이는 작업 디렉터리를 만들 수 있습니다. 디렉터리 이름의 대소문자를 실제 `mods/` 경로와 일치시켜야 합니다. 다음은 `AppleSkin`을 별도로 작업하는 예시입니다.

```sh
# josh clone https://github.com/coding-1ab/modpack-season-2.git <필터, 특정 경로만 받으려면 아래와 같이 적는다> ./AppleSkin
josh clone https://github.com/coding-1ab/modpack-season-2.git :/mods/AppleSkin ./AppleSkin
cd AppleSkin
```

변경 사항은 평소처럼 커밋하고 Josh로 중앙 저장소에 반영합니다. 다른 사람이 중앙 저장소를 갱신한 경우에는 먼저 필터링된 변경 사항을 가져옵니다.

```sh
josh changes pull
git add .
git commit -m "AppleSkin 수정"
josh push
```
