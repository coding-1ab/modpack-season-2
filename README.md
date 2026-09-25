# 모드팩 개발

모드 29개의 소스는 모두 이 저장소의 `mods/`에 있습니다. 전체 모드팩을 작업할 때는 저장소를 한 번만 복제하면 됩니다. 별도의 서브모듈 초기화는 필요하지 않습니다.

```sh
git clone https://github.com/coding-1ab/modpack-season-2.git
cd modpack-season-2
```

Gradle 데몬에는 Java 25가 필요합니다. 이 저장소에서 Gradle을 실행할 때 `JAVA_HOME`을 JDK 25 설치 경로로 지정합니다. 클라이언트 실행 명령은 다음과 같습니다.

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
cargo install josh-cli --locked --git https://github.com/josh-project/josh.git
josh clone https://github.com/coding-1ab/modpack-season-2.git :/mods/AppleSkin ./AppleSkin --branch master
cd AppleSkin
```

변경 사항은 평소처럼 커밋하고 Josh로 중앙 저장소에 반영합니다. 다른 사람이 중앙 저장소를 갱신한 경우에는 먼저 필터링된 변경 사항을 가져옵니다.

```sh
josh changes pull
git add .
git commit -m "AppleSkin 수정"
josh push
```

다른 모드도 `:/mods/AppleSkin`과 작업 디렉터리 이름을 해당 모드의 실제 경로로 바꾸면 됩니다. Josh CLI는 작업 디렉터리와 이력의 표시 범위를 필터링하지만, 중앙 저장소의 Git 객체 전체를 내려받습니다. 외부 모드 포크로 변경을 자동 반영하지 않습니다.

## 기존 모드 포크의 변경 사항

기존 모드 포크와 업스트림 저장소는 모노레포와 별개의 Git 저장소입니다. 포크의 변경 사항을 가져와야 할 때는 해당 저장소의 이력을 확인하고 필요한 변경을 중앙 저장소의 `mods/` 경로에 반영해야 합니다. Josh는 개별 모드 작업 디렉터리와 중앙 저장소 사이의 변경을 동기화하지만, 외부 포크를 자동으로 갱신하지는 않습니다.
