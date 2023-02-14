package Prettifier;

import lombok.SneakyThrows;
import org.eclipse.jgit.errors.IllegalTodoFileModification;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.RebaseTodoLine;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.*;

import static org.eclipse.jgit.api.RebaseCommand.InteractiveHandler;

public interface Chains {

    static void handle(RepositoryWrapper rw) {
        List<RevCommit> commits = rw.collectCommits();
        Map<RevCommit, Integer> childCount = new HashMap<>();

        for (RevCommit rc : commits) {
            for (RevCommit p : rc.getParents())
                childCount.put(p, childCount.getOrDefault(p, 0) + 1);
        }

        Set<RevCommit> squashable = new HashSet<>();

        for (RevCommit revCommit : commits) {
            RevCommit[] parents = revCommit.getParents();
            if (parents.length != 1)
                continue;
            RevCommit p = parents[0];

            if (1 == childCount.getOrDefault(p, 0) && 1 == childCount.getOrDefault(revCommit, 0) && p != commits.get(0))
                squashable.add(revCommit);
        }

        InteractiveHandler ih = new InteractiveHandler() {
            @Override
            @SneakyThrows(IllegalTodoFileModification.class)
            public void prepareSteps(List<RebaseTodoLine> steps) {
                for (var step : steps) {
                    AbbreviatedObjectId id = step.getCommit();
                    if (squashable.stream().anyMatch(revCommit -> id.prefixCompare(revCommit) == 0)) {
                        step.setAction(RebaseTodoLine.Action.FIXUP);
                        System.out.println(step + "sq");
                    }
                    else
                        System.out.println(step + "not sq");
                }
            }
            @Override
            public String modifyCommitMessage(String message) {
                return null;
            }
        };

        rw.rebuild(ih);
    }
}
