package CommitConsumers;

import org.eclipse.jgit.revwalk.RevCommit;

import Prettifier.Identity;

import java.time.Instant;
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

    public Instant minimalTime() {
        return commitList
                .stream()
                .flatMap(revCommit -> Stream.of(revCommit.getAuthorIdent().getWhenAsInstant(), revCommit.getCommitterIdent().getWhenAsInstant()))
                .min(Instant::compareTo)
                .get();
    }

    public Instant maximalTime() {
        return commitList
                .stream()
                .flatMap(revCommit -> Stream.of(revCommit.getAuthorIdent().getWhenAsInstant(), revCommit.getCommitterIdent().getWhenAsInstant()))
                .max(Instant::compareTo)
                .get();
    }
}
