import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.*;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class Main {

    public static void main(String[] args) throws IOException {
        File oldRepo = new File("GameLoader");
        File newRepo = new File("GameLoader2");;
        File newRepoGit = new File("GameLoader2/.git");

//        FileUtils.deleteDirectory(newRepo);
//        FileUtils.copyDirectory(oldRepo, newRepo);

        try (Repository r = new FileRepository(newRepoGit)) {
            RepositoryRebuilder rr = new RepositoryRebuilder(r);

            var cm = new Consumer<CommitBuilder>() {
                @Override
                public void accept(CommitBuilder commitBuilder) {

                }
            };

//            rr.visitCommits(new CommitDuplicator(r, cm));

            rr.visitCommits(new CommitPrinter(r));

//            CommitCollector cc = new CommitCollector();
//            rr.visitCommits(cc);
//            System.out.println(cc.collectIdents());
        }
    }
}
