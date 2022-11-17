import lombok.SneakyThrows;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class CommitPrinter implements Consumer<RevCommit> {
    private final Repository repository;

    public CommitPrinter(Repository repository) {
        this.repository = repository;
    }

    @SneakyThrows(IOException.class)
    @Override
    public void accept(RevCommit commit) {
        List<?> parents = Arrays.stream(commit.getParents()).map(RevObject::getId).toList();
        System.out.println(commit.getShortMessage()
                + " authored by " + commit.getAuthorIdent().getName()
                + " committed by " + commit.getCommitterIdent().getName()
                + " sha " + commit.getId()
                + " has parents: " + parents);

        RevTree treeId = commit.getTree();
        TreeWalk treeWalk = new TreeWalk(repository);
        treeWalk.addTree(treeId);

        treeWalk.setRecursive(true);
        while (treeWalk.next()) {
            System.out.println("\t" + treeWalk.getPathString());
        }
    }
}
