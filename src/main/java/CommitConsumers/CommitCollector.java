package CommitConsumers;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import java.lang.reflect.Array;
import Prettifier.Identity;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommitCollector implements Consumer<RevCommit> {
    private List<RevCommit> commitList = new ArrayList<>();

    @Override
    public void accept(RevCommit revCommit) {
        commitList.add(revCommit);
    }

    public Set<Identity> collectIdents() {
        return commitList
                .stream()
                .flatMap(revCommit -> Stream.of(revCommit.getAuthorIdent(), revCommit.getCommitterIdent()))
                .map(id -> new Identity(id.getName(), id.getEmailAddress()))
                .collect(Collectors.toSet());
    }
}
