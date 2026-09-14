# DEV-MAP-002 — 거점 구조물/템플릿 자동배치 파이프라인

## 목표
DEV-MAP-001의 10거점 논리맵을 데이터파일에서 읽어 GameState로 가져오고, 실제 개발 월드에서 이동·점령·GUI 테스트가 가능한 최소 거점 표식을 자동 배치한다.

## 구현
- `plugins/DantaServer/maps/dev-test-map.yml` 외부 복사본 자동 생성.
- YAML → `DevMapDefinition` 데이터 로더.
- 테스트 국가(red/blue), 10개 거점, 13개 간선을 GameState에 병합하는 `DevMapService`.
- 기존 ID가 있으면 중복 생성하지 않는 idempotent import.
- 국가 수도는 DEV 맵 정의의 수도 거점으로 연결.
- `MapStructurePlacer`가 각 거점 좌표에 5x5 석재 플랫폼 + 유형별 중앙 표식 + 횃불을 배치.
- 명시적 관리자 맵 초기화 작업에 한해서 대상 청크 로드를 허용한다. 일반 시설 동기화에서 비로드 청크를 강제 로드하는 원칙으로 확대하지 않는다.
- 논리맵 import 후 중요 Snapshot을 비동기로 즉시 Flush.

## 명령
- `/danta devmap status` — 데이터 정의/현재 GameState 상태 확인
- `/danta devmap load` — 논리 데이터만 import
- `/danta devmap place` — 월드 표식만 배치
- `/danta devmap apply` — import + 배치

권한: `danta.admin.map` (기본 OP)

## 배치 표식
- CAPITAL: BEACON
- FARM: HAY_BLOCK
- FORESTRY: OAK_LOG
- MINE/BARRACKS: IRON_BLOCK
- COMMERCIAL: EMERALD_BLOCK
- ACADEMIC: BOOKSHELF
- PORT: OAK_PLANKS
- GATE: CHISELED_STONE_BRICKS
- MAJOR: GOLD_BLOCK

이는 최종 건축물이 아니라 전략 이동/점령/AI/좌표 검증용 최소 템플릿이다.

## 수동 테스트
1. 서버 시작 후 PostgreSQL READY 확인.
2. `/danta devmap status`
3. `/danta devmap apply`
4. 출력에서 Points +10, Edges +13(기존 동일 ID가 있으면 existing으로 표시) 확인.
5. `/지도`에서 새 테스트맵 거점 확인.
6. `/tp 0 72 0` 등 테스트 좌표에서 플랫폼/거점 표식 확인.
7. 서버 재시작 후 `/지도`에서 논리맵이 Snapshot으로 복구되는지 확인.

Status: IMPLEMENTED — representative-side live test pending.
