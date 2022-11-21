package Prettifier;

import lombok.SneakyThrows;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.internal.storage.file.FileRepository;

import java.io.File;
import java.io.IOException;

public class Main {
    @SneakyThrows({IOException.class, InterruptedException.class})
    static String launchVim(String initialContent) {
        File tempFile = File.createTempFile("gitprettifier_test_file", ".txt");
        FileUtils.writeStringToFile(tempFile, initialContent);

        int returnValue = new ProcessBuilder()
                .inheritIO()
                .command("vim", tempFile.getAbsolutePath())
                .start()
                .waitFor();

        String content = FileUtils.readFileToString(tempFile);
        tempFile.delete();

        return returnValue == 0 ? content : "";
    }

    @SneakyThrows(ParseException.class)
    static CommandLine argsHandler(String[] args) {
        Options options = new Options();

        options.addOption(
                Option.builder("input")
                        .argName("input")
                        .required(true)
                        .hasArg(true)
                        .valueSeparator('=')
                        .desc("Location of input repo")
                        .build()
        );

        options.addOption(
                Option.builder("output")
                        .argName("output")
                        .required(false)
                        .hasArg(true)
                        .valueSeparator('=')
                        .desc("Location of output repo")
                        .build()
        );

        options.addOption(
                Option.builder("authors")
                        .argName("authors")
                        .required(false)
                        .hasArg(false)
                        .desc("Change authors / committers")
                        .build()
        );

        options.addOption(
                Option.builder("map_time")
                        .argName("map_time")
                        .required(false)
                        .numberOfArgs(2)
                        .desc("Change commit time")
                        .build()
        );

        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args, false);
    }

    @SneakyThrows(IOException.class)
    static void cmdHandler(CommandLine cmd) {
        String input = cmd.getOptionValue("input");
        String output = cmd.hasOption("output") ? cmd.getOptionValue("output") : input;

        File oldRepo = new File(input);
        File newRepo = new File(output);

        if (!oldRepo.exists()) {
            System.err.println("input repo does not exist");
            return;
        }

        if (!oldRepo.equals(newRepo)) {
            if (newRepo.exists())
                FileUtils.deleteDirectory(newRepo);
            FileUtils.copyDirectory(oldRepo, newRepo);
        }

        File newRepoGit = newRepo.listFiles((dir, name) -> ".git".equals(name))[0];
        RepositoryWrapper rw = new RepositoryWrapper(new FileRepository(newRepoGit));

        if (cmd.hasOption("authors"))
            Authors.handle(rw);
        else if (cmd.hasOption("map_time"))
            Time.handle(rw, cmd.getOptionValues("map_time")[0], cmd.getOptionValues("map_time")[1]);
    }

    public static void main(String[] args) {
        CommandLine cmd = argsHandler(args);
        cmdHandler(cmd);
    }
}
