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

