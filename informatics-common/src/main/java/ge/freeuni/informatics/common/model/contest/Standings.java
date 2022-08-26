package ge.freeuni.informatics.common.model.contest;

import java.util.ArrayList;
import java.util.List;

public class Standings {

    private final List<ContestantResult> standings = new ArrayList<>();

    public List<ContestantResult> getStandings() {
        return standings;
    }
}
