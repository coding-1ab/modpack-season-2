# 빌드
1. 이 리포지토리와 서브모듈을 내려받는다. 이미 git pull로 받았다면 아래 명령어로 서브모듈을 내려받을 수 있다:
```sh
git submodule update --recursive --init --remote
```

2. 각 서브모듈의 HEAD를 브랜치에 부착한다. `attach.py`를 실행하면 알아서 해준다:
```sh
python3 attach.py
```

3. 마인크래프트를 실행하려면 루트에서 아래 명령어를 친다:
```sh
# 모장 로그인 없이 실행
./gradlew runClient

# 모장 로그인하고 실행
./gradlew runClientAuth
```
주의사항: 기본적으로 parallel execution이 활성화 되어있기 때문에 컴퓨터 코어 수가 많으면 메모리를 전부 사용할 수 있음.
gradle.properties에서 parallel execution을 임시로 비활성화 할 수 있음.

# 새로운 모드 소스 추가
추가하는 모든 모드의 소스의 서브 모듈은 무조건 branch가 설정되어야만 함.
`attach.py`가 동작하기 위해선 .gitmodules에 브랜치각 설정되어야만 하기 때문.

# 업데이트 및 문제 해결
만약 다른 기여자가 새로운 모드 소스를 추가한다면 그 서브 모듈이 이상하게 클론되어 실행이 안될 수 있음.
아니면 이미 클론한 모드 소스의 리모트 URL이 변경된다면 그 서브 모듈에 이게 제대로 반영이 안될 수 있음.
그럴 때는 `try_update_and_fix.py`를 실행해볼 것.
(아마도) 문제가 해결될 것임.

# 모드 업스트림 확인
모드의 업스트림 소스에 새로운 커밋이 있는지 확인하려면 `check_update.py`를 실행하면 된다. 아래와 같은 출력이 나오면 받아올 커밋이 있는 것이다:
```
+git fetch upstream (in mods/Veil)

+git log --oneline origin/1.21..upstream/1.21 (in mods/Veil)
업스트림에서 받아와야 하는 새로운 커밋들:
8f5db3b13 Add maven and discord badge icons
297261046 Update README with Modrinth and CurseForge links
76cc9c148 Fix crash with sodium
cdf04a166 Add VeilRegisterInspectorsEvent
d72219b74 Add command for controlling post processing
df732f1cd Add ImGuiMC repo
0212f03e2 Update publishing
1cede3c1d Update changelog.md
4a102b271 Update loom
6985c7500 Remove unused resources
76dbd98d6 Move test packs to example mod
45293978f Update changelog.md
0de6aa712 Remove deprecated features
c1a92615d Remove ImGui hard dependency
eca242f9f Migrate to imguimc
fb8132930 Update VeilMixinPlugin.java
```
이럴 때는 `mods/Veil`에서 업스트림을 병합하면 된다.
