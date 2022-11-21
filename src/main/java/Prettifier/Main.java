package Prettifier;

import CommitModifiers.CommitConstantTimeShifter;
import CommitModifiers.CommitIdentityMapper;
import lombok.SneakyThrows;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

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

        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args, false);
    }

    static void cmdHandler(CommandLine cmd) {
//        System.err.println("cmdHandler()");

        if (cmd.hasOption("authors"))
            Authors.handle(cmd);
    }

    public static void main(String[] args) {
        CommandLine cmd = argsHandler(args);
        cmdHandler(cmd);
    }
}
