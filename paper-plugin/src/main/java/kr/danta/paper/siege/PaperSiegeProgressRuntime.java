package kr.danta.paper.siege;

import kr.danta.core.siege.SiegeEngagementProfile;
import kr.danta.core.siege.SiegeProgress;
import kr.danta.core.siege.SiegeProgressEvent;
import kr.danta.core.siege.SiegeStage;
import kr.danta.core.territory.StrategicPointType;
import kr.danta.core.snapshot.SiegeProgressSnapshot;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * DEV-113 Paper-facing in-memory bridge for staged siege progression.
 * Real combat/gate listeners can feed this runtime without duplicating core siege rules.
 */
public final class PaperSiegeProgressRuntime {
    private final Map<String, SiegeProgress> progressByPointId = new LinkedHashMap<>();
    private final Map<String, Boolean> resumeRequiredByPointId = new LinkedHashMap<>();

    public SiegeEngagementProfile profileFor(StrategicPointType pointType) {
        Objects.requireNonNull(pointType, "pointType");
        return switch (pointType) {
            case CAPITAL -> SiegeEngagementProfile.CAPITAL_THREE_BATTLE;
            case MAJOR -> SiegeEngagementProfile.MAJOR_SINGLE_BATTLE;
            default -> SiegeEngagementProfile.NORMAL_QUICK;
        };
    }

    public void start(String pointId, StrategicPointType pointType) {
        String id = requirePointId(pointId);
        if (progressByPointId.containsKey(id)) {
            throw new IllegalStateException("해당 거점에는 이미 진행 중인 공성이 있습니다.");
        }
        progressByPointId.put(id, new SiegeProgress(profileFor(pointType)));
        resumeRequiredByPointId.put(id, false);
    }

    public void recordBattleWin(String pointId) {
        SiegeProgress progress = requireProgress(pointId);
        try {
            progress.apply(SiegeProgressEvent.BATTLE_WON);
            resumeRequiredByPointId.put(requirePointId(pointId), false);
        } catch (IllegalStateException ex) {
            throw new IllegalStateException("현재 단계에서는 전투 승리를 처리할 수 없습니다. " + status(pointId), ex);
        }
    }

    public void recordGateBreach(String pointId) {
        SiegeProgress progress = requireProgress(pointId);
        try {
            progress.apply(SiegeProgressEvent.GATE_BREACHED);
            resumeRequiredByPointId.put(requirePointId(pointId), false);
        } catch (IllegalStateException ex) {
            throw new IllegalStateException("현재 단계에서는 성문을 파괴할 수 없습니다. " + status(pointId), ex);
        }
    }

    public void recordQuickResolution(String pointId) {
        SiegeProgress progress = requireProgress(pointId);
        try {
            progress.apply(SiegeProgressEvent.QUICK_RESOLVED);
            resumeRequiredByPointId.put(requirePointId(pointId), false);
        } catch (IllegalStateException ex) {
            throw new IllegalStateException("현재 단계에서는 일반 거점의 빠른 점령을 처리할 수 없습니다. " + status(pointId), ex);
        }
    }

    public String status(String pointId) {
        SiegeProgress progress = requireProgress(pointId);
        return "[공성 진행] " + stageText(progress.stage())
                + (resumeRequired(pointId) ? " §6[재개 대기]" : "");
    }

    public boolean complete(String pointId) {
        return requireProgress(pointId).complete();
    }

    public boolean active(String pointId) {
        String id = requirePointId(pointId);
        SiegeProgress progress = progressByPointId.get(id);
        return progress != null && !progress.complete();
    }

    public boolean resumeRequired(String pointId) {
        return resumeRequiredByPointId.getOrDefault(requirePointId(pointId), false);
    }

    public java.util.List<SiegeProgressSnapshot> snapshots() {
        return progressByPointId.entrySet().stream()
                .map(entry -> new SiegeProgressSnapshot(
                        entry.getKey(),
                        entry.getValue().profile(),
                        entry.getValue().stage(),
                        resumeRequiredByPointId.getOrDefault(entry.getKey(), false)))
                .toList();
    }

    public void restore(SiegeProgressSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        String id = requirePointId(snapshot.pointId());
        progressByPointId.put(id, SiegeProgress.restored(snapshot.profile(), snapshot.stage()));
        resumeRequiredByPointId.put(id, snapshot.resumeRequired());
    }

    public void clearAll() {
        progressByPointId.clear();
        resumeRequiredByPointId.clear();
    }

    public void clear(String pointId) {
        String id = requirePointId(pointId);
        progressByPointId.remove(id);
        resumeRequiredByPointId.remove(id);
    }

    private SiegeProgress requireProgress(String pointId) {
        String id = requirePointId(pointId);
        SiegeProgress progress = progressByPointId.get(id);
        if (progress == null) {
            throw new IllegalStateException("해당 거점에서 진행 중인 공성이 없습니다.");
        }
        return progress;
    }

    private static String requirePointId(String pointId) {
        Objects.requireNonNull(pointId, "pointId");
        String normalized = pointId.trim();
        if (normalized.isEmpty()) throw new IllegalArgumentException("pointId must not be blank");
        return normalized;
    }

    private static String stageText(SiegeStage stage) {
        return switch (stage) {
            case QUICK_RESOLUTION -> "일반 거점 빠른 점령 대기";
            case SINGLE_BATTLE -> "중요 거점 결정 전투";
            case OUTER_BATTLE -> "외성 전투";
            case OUTER_GATE -> "외성 성문 파괴";
            case PLAZA_BATTLE -> "광장 전투";
            case INNER_GATE -> "내성문 파괴";
            case CORE_BATTLE -> "핵심 전투";
            case COMPLETE -> "공성 완료";
        };
    }
}
