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
import java.util.Set;
import java.util.function.Consumer;

public class CommitSquasher implements Consumer<RevCommit> {
    private final Repository repository;
    private final Set<RevCommit> squashOn;

    final Map<ObjectId, ObjectId> mapped = new HashMap<>();
    final Map<ObjectId, ObjectId[]> parentsOf = new HashMap<>();
    private ObjectId lastCreated;

    public CommitSquasher(Repository repository, Set<RevCommit> squashOn) {
        this.repository = repository;
        this.squashOn = squashOn;
    }

    public ObjectId getLastCreated() {
        return lastCreated;
    }

    @SneakyThrows
    @Override
    public void accept(RevCommit oldCommit) {
        CommitBuilder builder = new CommitBuilder();
        ObjectId[] parents;

        if (oldCommit.getParentCount() != 1 || !squashOn.contains(oldCommit.getParent(0))) {
            parents = Arrays
                    .stream(oldCommit.getParents())
                    .map(parentCommit -> mapped.get(parentCommit.getId()))
                    .toArray(ObjectId[]::new);
        }
        else {
            ObjectId parentOrigId = oldCommit.getParent(0).getId();
            ObjectId parentNewId = mapped.get(parentOrigId);

            parents = parentsOf.get(parentNewId);
        }

        ObjectId treeId = oldCommit.getTree().getId();
        builder.setTreeId(treeId);

        builder.setParentIds(parents);

        builder.setAuthor(oldCommit.getAuthorIdent());
        builder.setCommitter(oldCommit.getCommitterIdent());
        builder.setMessage(oldCommit.getFullMessage());
        builder.setEncoding(oldCommit.getEncoding());

        ObjectInserter inserter = repository.newObjectInserter();
        ObjectId newId = inserter.insert(builder);

        mapped.put(oldCommit.getId(), newId);
        parentsOf.put(newId, parents);
        lastCreated = newId;
    }
}
