package CommitConsumers;

import lombok.SneakyThrows;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CommitTreeifier implements Consumer<RevCommit> {
    private final Repository repository;

    final Map<ObjectId, ObjectId> mapped = new HashMap<>();
    final Map<ObjectId, Integer> pathLen = new HashMap<>();
    private ObjectId lastCreated;

    public CommitTreeifier(Repository repository) {
        this.repository = repository;
    }

    public ObjectId getLastCreated() {
        return lastCreated;
    }

    @SneakyThrows
    @Override
    public void accept(RevCommit oldCommit) {
        CommitBuilder builder = new CommitBuilder();

        int mx = 0;
        ObjectId par = null;

        for (RevCommit p : oldCommit.getParents()) {
            ObjectId parentNewId = mapped.get(p.getId());
            int pathHere = pathLen.get(parentNewId);

            if (pathHere > mx) {
                mx = pathHere;
                par = parentNewId;
            }
        }

        if (par == null)
            builder.setParentIds();
        else
            builder.setParentId(par);

        ObjectId treeId = oldCommit.getTree().getId();
        builder.setTreeId(treeId);

        builder.setAuthor(oldCommit.getAuthorIdent());
        builder.setCommitter(oldCommit.getCommitterIdent());
        builder.setMessage(oldCommit.getFullMessage());
        builder.setEncoding(oldCommit.getEncoding());

        ObjectInserter inserter = repository.newObjectInserter();
        ObjectId newId = inserter.insert(builder);

        mapped.put(oldCommit.getId(), newId);
        pathLen.put(newId, mx + 1);
        lastCreated = newId;
    }
}
