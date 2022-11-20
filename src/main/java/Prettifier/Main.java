package Prettifier;

import CommitModifiers.CommitConstantTimeShifter;
import CommitModifiers.CommitIdentityMapper;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.*;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;

public class Main {
    static void mishandle(RepositoryWrapper r) {
        CommitIdentityMapper cim = new CommitIdentityMapper(id -> {
            return new Identity("Kacper Topolski", "kacpertopolski@op.pl");
        });

        CommitConstantTimeShifter ccts = new CommitConstantTimeShifter(
                Duration.ofHours(24 * 162 - 4)
        );

        r.rebuild(cim, ccts);
    }

    @SneakyThrows(IOException.class)
    public static void main(String[] args) {
        System.out.println(Arrays.toString(args));

        File oldRepo = new File("chess2");
        File newRepo = new File("chess");;
        File newRepoGit = new File("chess/.git");

        FileUtils.deleteDirectory(newRepo);
        FileUtils.copyDirectory(oldRepo, newRepo);

        try (Repository r = new FileRepository(newRepoGit)) {
            mishandle(new RepositoryWrapper(r));
        }
    }
}
