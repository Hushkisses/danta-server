package kr.danta.core.snapshot;
import kr.danta.core.diplomacy.DiplomaticStatus;
public record DiplomaticRelationSnapshot(String nationAId,String nationBId,DiplomaticStatus status) {}
