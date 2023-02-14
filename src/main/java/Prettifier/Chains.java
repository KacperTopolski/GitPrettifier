package Prettifier;

import CommitConsumers.CommitSquasher;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface Chains {
    static void handle(RepositoryWrapper rw) {
        List<RevCommit> commits = rw.collectCommits();
        Map<RevCommit, Integer> childCount = new HashMap<>();

        for (RevCommit rc : commits) {
            for (RevCommit p : rc.getParents())
                childCount.put(p, childCount.getOrDefault(p, 0) + 1);
        }

        Set<RevCommit> squashOn = commits
                .stream()
                .filter(revCommit -> childCount.getOrDefault(revCommit, 0) == 1)
                .collect(Collectors.toSet());

        CommitSquasher cs = new CommitSquasher(rw.unwrap(), squashOn);
        commits.forEach(cs);

        rw.rebuild(cs.getLastCreated());
    }
}
