package Prettifier;

import lombok.SneakyThrows;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.internal.storage.file.FileRepository;

import java.io.File;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    @SneakyThrows
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

    static Options createOptions() {
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
                        .desc("Changes authors / committers")
                        .build()
        );

        options.addOption(
                Option.builder("time")
                        .argName("time")
                        .required(false)
                        .numberOfArgs(2)
                        .desc("Changes commit time")
                        .build()
        );

        options.addOption(
                Option.builder("chains")
                        .argName("chains")
                        .required(false)
                        .desc("Squashes all chains")
                        .build()
        );

        options.addOption(
                Option.builder("tree")
                        .argName("tree")
                        .required(false)
                        .desc("Picks longest path from DAG of commits")
                        .build()
        );

        return options;
    }

    @SneakyThrows
    static void cmdHandler(CommandLine cmd) {
        String input = cmd.getOptionValue("input");
        String output = cmd.hasOption("output") ? cmd.getOptionValue("output") : input;

        File oldRepo = new File(input);
        File newRepo = new File(output);

        if (!oldRepo.exists()) {
            System.err.println("Input repository does not exist");
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
        else if (cmd.hasOption("time"))
            Time.handle(rw, cmd.getOptionValues("time")[0], cmd.getOptionValues("time")[1]);
        else if (cmd.hasOption("chains"))
            Chains.handle(rw);
        else if (cmd.hasOption("tree"))
            Tree.handle(rw);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            Scanner input = new Scanner(System.in);
            args = input.nextLine().split("\\s+");
        }

        Options options = createOptions();
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args, false);
            cmdHandler(cmd);
        } catch (ParseException pe) {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("GitPrettifier", options);
            System.out.println(pe.getMessage());
        }
    }
}
