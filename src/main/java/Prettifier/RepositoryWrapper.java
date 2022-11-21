package Prettifier;

import CommitConsumers.CommitDuplicator;
import lombok.SneakyThrows;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class RepositoryWrapper {
    private final Repository repository;

    public RepositoryWrapper(Repository repository) {
        this.repository = repository;
    }

    @SneakyThrows(IOException.class)
    private List<RevCommit> collectCommits() {
        ObjectId head = repository.resolve("HEAD");
        RevCommit headCommit = repository.parseCommit(head);

        RevWalk revWalk = new RevWalk(repository);
        revWalk.markStart(headCommit);

        List<RevCommit> commitList = new ArrayList<>();
        revWalk.forEach(commitList::add);

        Collections.reverse(commitList);
        return commitList;
    }

    public <T extends Consumer<RevCommit>> T visitCommits(T commitConsumer) {
        collectCommits().forEach(commitConsumer);
        return commitConsumer;
    }

    public Repository unwrap() {
        return repository;
    }

    // assumes repo is checked out
    @SneakyThrows({IOException.class, GitAPIException.class})
    public void rebuild(Consumer<CommitBuilder>... modifiers) {
        CommitDuplicator cd = new CommitDuplicator(repository, modifiers);
        visitCommits(cd);

        Git g = new Git(repository);

        String finalBranchName = repository.getBranch(), temporalBranchName = "temporal_42";

        g.branchCreate()
                .setStartPoint(cd.getLastCreated().name())
                .setName(temporalBranchName)
                .call();

        g.checkout()
                .setName(temporalBranchName)
                .call();

        g.branchDelete()
                .setBranchNames(finalBranchName)
                .setForce(true)
                .call();

        g.branchRename()
                .setOldName(temporalBranchName)
                .setNewName(finalBranchName)
                .call();
    }
}
