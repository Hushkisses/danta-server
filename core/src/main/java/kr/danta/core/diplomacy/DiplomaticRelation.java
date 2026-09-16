package kr.danta.core.diplomacy;
import java.util.Objects;
public record DiplomaticRelation(String nationAId, String nationBId, DiplomaticStatus status) {
 public DiplomaticRelation { nationAId=req(nationAId); nationBId=req(nationBId); status=Objects.requireNonNull(status); if(nationAId.equals(nationBId)) throw new IllegalArgumentException("self relation is not allowed"); if(nationAId.compareTo(nationBId)>0){String t=nationAId;nationAId=nationBId;nationBId=t;} }
 private static String req(String s){Objects.requireNonNull(s);s=s.trim();if(s.isEmpty())throw new IllegalArgumentException("nation id is blank");return s;}
 public boolean involves(String id){return nationAId.equals(id)||nationBId.equals(id);}
}
