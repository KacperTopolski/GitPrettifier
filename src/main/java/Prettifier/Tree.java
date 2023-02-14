package Prettifier;

import CommitConsumers.CommitTreeifier;
import lombok.SneakyThrows;

public interface Tree {
    @SneakyThrows
    static void handle(RepositoryWrapper rw) {
        CommitTreeifier ct = new CommitTreeifier(rw.unwrap());
        rw.visitCommits(ct);

        rw.rebuild(ct.getLastCreated());
    }
}
