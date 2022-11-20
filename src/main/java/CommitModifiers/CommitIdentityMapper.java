package CommitModifiers;

import Prettifier.Identity;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.PersonIdent;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class CommitIdentityMapper implements Consumer<CommitBuilder> {
    private final Function<? super Identity, ? extends Identity> mapper;

    public CommitIdentityMapper(Function<? super Identity, ? extends Identity> f) {
        mapper = f;
    }
    public CommitIdentityMapper(Map<? super Identity, ? extends Identity> m) {
        mapper = m::get;
    }

    @Override
    public void accept(CommitBuilder commitBuilder) {
        PersonIdent pid = commitBuilder.getAuthor();
        Identity id = new Identity(pid.getName(), pid.getEmailAddress());

        id = mapper.apply(id);
        commitBuilder.setAuthor(new PersonIdent(id.name(), id.emailAddress(), pid.getWhen(), pid.getTimeZone()));

        pid = commitBuilder.getCommitter();
        id = new Identity(pid.getName(), pid.getEmailAddress());

        id = mapper.apply(id);
        commitBuilder.setCommitter(new PersonIdent(id.name(), id.emailAddress(), pid.getWhen(), pid.getTimeZone()));
    }
}
