package kr.danta.core.siege;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * DEV-118 in-memory participation state. Persistence is intentionally deferred to DEV-120.
 */
public final class SiegeParticipantRegistry {
    private final SiegeCommanderCasualtyPolicy casualtyPolicy;
    private final Map<Key, Participant> participants = new HashMap<>();
    private final Map<SideKey, Integer> moraleDeltaBySide = new HashMap<>();

    public SiegeParticipantRegistry(SiegeCommanderCasualtyPolicy casualtyPolicy) {
        this.casualtyPolicy = Objects.requireNonNull(casualtyPolicy, "casualtyPolicy");
    }

    public synchronized void join(String pointId, UUID playerId, SiegeSide side) {
        String point = requirePoint(pointId);
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(side, "side");
        Key key = new Key(point, playerId);
        Participant existing = participants.get(key);
        if (existing != null && existing.eliminated()) {
            throw new IllegalStateException("해당 공성전에서는 다시 참전할 수 없습니다.");
        }
        if (existing != null && existing.side() != side) {
            throw new IllegalStateException("공성 중에는 진영을 변경할 수 없습니다.");
        }
        participants.put(key, new Participant(side, false));
    }

    public synchronized SiegeCommanderCasualty eliminate(String pointId, UUID playerId) {
        String point = requirePoint(pointId);
        Objects.requireNonNull(playerId, "playerId");
        Key key = new Key(point, playerId);
        Participant participant = participants.get(key);
        if (participant == null) throw new IllegalStateException("해당 플레이어는 이 공성전에 참가 중이 아닙니다.");
        if (participant.eliminated()) throw new IllegalStateException("이미 해당 공성전에서 탈락한 지휘관입니다.");

        participants.put(key, new Participant(participant.side(), true));
        SiegeCommanderCasualty casualty = casualtyPolicy.casualty(point, playerId, participant.side());
        SideKey sideKey = new SideKey(point, participant.side());
        moraleDeltaBySide.merge(sideKey, casualty.moraleDelta(), Integer::sum);
        return casualty;
    }

    public synchronized boolean participating(String pointId, UUID playerId) {
        return participants.containsKey(new Key(requirePoint(pointId), Objects.requireNonNull(playerId, "playerId")));
    }

    public synchronized boolean eliminated(String pointId, UUID playerId) {
        Participant participant = participants.get(new Key(requirePoint(pointId), Objects.requireNonNull(playerId, "playerId")));
        return participant != null && participant.eliminated();
    }

    public synchronized SiegeSide side(String pointId, UUID playerId) {
        Participant participant = participants.get(new Key(requirePoint(pointId), Objects.requireNonNull(playerId, "playerId")));
        if (participant == null) throw new IllegalStateException("해당 플레이어는 이 공성전에 참가 중이 아닙니다.");
        return participant.side();
    }

    public synchronized int moraleDelta(String pointId, SiegeSide side) {
        return moraleDeltaBySide.getOrDefault(new SideKey(requirePoint(pointId), Objects.requireNonNull(side, "side")), 0);
    }

    public synchronized void clear(String pointId) {
        String point = requirePoint(pointId);
        participants.keySet().removeIf(key -> key.pointId().equals(point));
        moraleDeltaBySide.keySet().removeIf(key -> key.pointId().equals(point));
    }

    private static String requirePoint(String pointId) {
        Objects.requireNonNull(pointId, "pointId");
        String normalized = pointId.trim();
        if (normalized.isEmpty()) throw new IllegalArgumentException("pointId must not be blank");
        return normalized;
    }

    private record Key(String pointId, UUID playerId) {}
    private record SideKey(String pointId, SiegeSide side) {}
    private record Participant(SiegeSide side, boolean eliminated) {}
}
