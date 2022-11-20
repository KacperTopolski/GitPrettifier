package CommitConsumers;

import lombok.SneakyThrows;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CommitDuplicator implements Consumer<RevCommit> {
    private final Consumer<CommitBuilder>[] commitModifiers;
    private final Repository repository;

    final Map<ObjectId, ObjectId> mapped = new HashMap<>();
    private ObjectId lastCreated;

    public CommitDuplicator(Repository repository, Consumer<CommitBuilder>[] commitModifiers) {
        this.commitModifiers = commitModifiers;
        this.repository = repository;
    }

    @SneakyThrows
    @Override
    public void accept(RevCommit oldCommit) {
        CommitBuilder builder = new CommitBuilder();

        ObjectId treeId = oldCommit.getTree().getId();
        builder.setTreeId(treeId);

        ObjectId[] parents = Arrays
                .stream(oldCommit.getParents())
                .map(parentCommit -> mapped.get(parentCommit.getId()))
                .toArray(ObjectId[]::new);
        builder.setParentIds(parents);

        builder.setAuthor(oldCommit.getAuthorIdent());
        builder.setCommitter(oldCommit.getCommitterIdent());
        builder.setMessage(oldCommit.getFullMessage());
        builder.setEncoding(oldCommit.getEncoding());

        for (Consumer<CommitBuilder> ccb : commitModifiers)
            ccb.accept(builder);

        ObjectInserter inserter = repository.newObjectInserter();
        ObjectId newId = inserter.insert(builder);

        System.out.println(newId + " from " + oldCommit.getId());

        mapped.put(oldCommit.getId(), newId);
        lastCreated = newId;
    }

    public ObjectId getLastCreated() {
        return lastCreated;
    }
}
