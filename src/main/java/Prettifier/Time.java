package Prettifier;

import CommitConsumers.CommitCollector;
import CommitModifiers.CommitTimeShifter;

import java.time.Instant;

public interface Time {
    static Instant toInstant(String s) {
        String proper = "2007-12-03T10:15:30.00Z";

        return Instant.parse(s + proper.substring(s.length()));
    }

    static void handle(RepositoryWrapper rw, String from, String to) {
        CommitCollector cc = rw.visitCommits(new CommitCollector());

        Instant from_i = toInstant(from);
        Instant to_i = toInstant(to);

        if (!from_i.isBefore(to_i)) {
            Instant swp = from_i;
            from_i = to_i;
            to_i = swp;
        }

        rw.rebuild(new CommitTimeShifter(cc.minimalTime(), cc.maximalTime(), from_i, to_i));
    }
}
