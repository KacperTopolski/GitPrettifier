package CommitModifiers;

import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.PersonIdent;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.function.Function;

public class CommitTimeShifter implements Consumer<CommitBuilder> {
    private Function<Instant, Instant> f;

    public CommitTimeShifter(Duration shift) {
        f = instant -> instant.plus(shift);
    }
    public CommitTimeShifter(Instant oldl, Instant oldr, Instant newl, Instant newr) {
        double old_len = oldr.getEpochSecond() - oldl.getEpochSecond();
        double new_len = newr.getEpochSecond() - newl.getEpochSecond();

        f = i -> {
            double diff = i.getEpochSecond() - oldl.getEpochSecond();
            diff *= new_len / old_len;
            long res = (long) (newl.getEpochSecond() + diff);
            return i.plusSeconds(res - i.getEpochSecond());
        };
    }

    @Override
    public void accept(CommitBuilder commitBuilder) {
        PersonIdent pid = commitBuilder.getAuthor();
        commitBuilder.setAuthor(new PersonIdent(pid.getName(), pid.getEmailAddress(), f.apply(pid.getWhenAsInstant()), pid.getZoneId()));

        pid = commitBuilder.getCommitter();
        commitBuilder.setCommitter(new PersonIdent(pid.getName(), pid.getEmailAddress(), f.apply(pid.getWhenAsInstant()), pid.getZoneId()));
    }
}
