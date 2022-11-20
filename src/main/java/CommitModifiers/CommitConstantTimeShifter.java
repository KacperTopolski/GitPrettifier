package CommitModifiers;

import Prettifier.Identity;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.PersonIdent;

import java.time.Duration;
import java.util.function.Consumer;

public class CommitConstantTimeShifter implements Consumer<CommitBuilder> {
    private Duration shift;

    public CommitConstantTimeShifter(Duration shift) {
        this.shift = shift;
    }

    @Override
    public void accept(CommitBuilder commitBuilder) { // TODO clean this and identity mapper
        PersonIdent pid = commitBuilder.getAuthor();
        commitBuilder.setAuthor(new PersonIdent(pid.getName(), pid.getEmailAddress(), pid.getWhenAsInstant().plus(shift), pid.getZoneId()));

        pid = commitBuilder.getCommitter();
        commitBuilder.setCommitter(new PersonIdent(pid.getName(), pid.getEmailAddress(), pid.getWhenAsInstant().plus(shift), pid.getZoneId()));
    }
}
