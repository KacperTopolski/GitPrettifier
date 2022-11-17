import lombok.SneakyThrows;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.IOException;
import java.sql.SQLClientInfoException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class RepositoryRebuilder {
    private final Repository repository;

    public RepositoryRebuilder(Repository repository) {
        this.repository = repository;
    }

    @SneakyThrows(IOException.class)
    public List<RevCommit> collectCommits() {
        ObjectId head = repository.resolve("HEAD");
        RevCommit headCommit = repository.parseCommit(head);

        RevWalk revWalk = new RevWalk(repository);
        revWalk.markStart(headCommit);

        List<RevCommit> commitList = new ArrayList<>();
        revWalk.forEach(commitList::add);

        Collections.reverse(commitList);
        return commitList;
    }

    public void visitCommits(Consumer<RevCommit> commitConsumer) {
        collectCommits().forEach(commitConsumer);
    }
}
