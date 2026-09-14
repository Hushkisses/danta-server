# DEV-MAP-001 — 10거점 테스트 전략맵 설계

## 목표
실제 이동·점령·연결망·보급·GUI 검증이 가능한 최소 10거점 논리 전략맵을 정의한다. 화려한 건축보다 전략 시스템 검증을 우선한다.

## 설계 원칙
- 2개 수도 + 8개 일반/특수 거점으로 구성한다.
- 양측 수도에서 중앙으로 2개 이상 진출축이 생기게 한다.
- 단일 최단루트만 존재하지 않도록 우회 연결을 둔다.
- ROAD / PLAIN / FOREST_PATH / MOUNTAIN_PASS / CANYON 태그를 섞어 이후 DEV-032 이동시간·전장특성 테스트에 사용한다.
- 좌표는 개발 월드 기준 임시 배치값이며, DEV-MAP-002 구조물 자동배치 파이프라인 검증용이다.

## 거점 10개
| ID | 표시명 | 유형 | 좌표 (world) | 시설 슬롯 | 기본 생산/h | 초기 소유 |
|---|---|---|---:|---:|---|---|
| red_capital | 적국 수도 | CAPITAL | 0,70,0 | 3 | - | red |
| red_farm | 적국 농업촌 | FARM | 180,70,40 | 2 | food 200 | red |
| red_mine | 적국 광산촌 | MINE | 170,72,-130 | 2 | iron 120 | red |
| west_gate | 서부 관문 | GATE | 360,76,-20 | 3 | - | neutral |
| forest_crossing | 숲 교차로 | FORESTRY | 520,72,130 | 2 | wood 160 | neutral |
| central_market | 중앙 상업도시 | COMMERCIAL | 640,70,0 | 3 | gold 160 | neutral |
| canyon_fort | 협곡 요새 | GATE | 790,78,-120 | 3 | - | neutral |
| blue_farm | 청국 농업촌 | FARM | 970,70,60 | 2 | food 200 | blue |
| blue_mine | 청국 광산촌 | MINE | 960,72,-130 | 2 | iron 120 | blue |
| blue_capital | 청국 수도 | CAPITAL | 1140,70,0 | 3 | - | blue |

## 간선
| ID | A | B | 기본 이동 | 태그 |
|---|---|---|---:|---|
| e01 | red_capital | red_farm | 120s | ROAD,PLAIN |
| e02 | red_capital | red_mine | 150s | ROAD,PLAIN |
| e03 | red_farm | west_gate | 150s | ROAD,PLAIN |
| e04 | red_mine | west_gate | 180s | MOUNTAIN_PASS |
| e05 | west_gate | forest_crossing | 150s | FOREST_PATH |
| e06 | west_gate | central_market | 210s | ROAD,PLAIN |
| e07 | forest_crossing | central_market | 150s | FOREST_PATH |
| e08 | central_market | canyon_fort | 180s | CANYON |
| e09 | central_market | blue_farm | 210s | ROAD,PLAIN |
| e10 | canyon_fort | blue_mine | 150s | MOUNTAIN_PASS |
| e11 | blue_farm | blue_capital | 120s | ROAD,PLAIN |
| e12 | blue_mine | blue_capital | 150s | ROAD,PLAIN |
| e13 | canyon_fort | blue_farm | 180s | ROAD |

## 플레이 구조
- Red는 `red_farm / red_mine` 두 방향으로 출발할 수 있다.
- Blue도 `blue_farm / blue_mine` 두 방향으로 출발할 수 있다.
- `west_gate`는 Red 측의 첫 병목, `canyon_fort`는 Blue 측의 첫 병목 역할을 한다.
- `forest_crossing`은 중앙 우회축을 만들어 관문 하나만 뚫는 정답 루트를 방지한다.
- `central_market`은 테스트맵의 핵심 중앙 거점으로 두 진출축이 합류한다.
- 향후 보급/고립 구현 시 `west_gate`, `central_market`, `canyon_fort`를 끊었을 때 후방 고립 테스트가 가능하다.

## 데이터 파일
논리맵은 `paper-plugin/src/main/resources/maps/dev-test-map.yml`로 데이터화한다. DEV-MAP-002는 이 파일의 좌표/타입을 읽어 월드 구조물을 자동 배치하는 방향으로 진행한다.

## 완료조건
- 10개 거점과 13개 간선이 명시돼 있다.
- 양 수도 사이에 복수 경로가 존재한다.
- 현재 StrategicPoint/StrategicEdge 모델로 표현 가능하다.
- 이후 DEV-032 이동시간/전장특성 테스트에 사용할 태그가 포함돼 있다.

Status: COMPLETE
