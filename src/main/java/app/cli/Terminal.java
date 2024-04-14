package app.cli;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Scanner;

public class Terminal {
    private final Parser parser;
    private final ArrayList<String> commands;
    private Path currPath;

    public Terminal() {
        this.parser = new Parser();
        this.commands = new ArrayList<>();
        this.currPath = Paths.get("").toAbsolutePath();
    }
    
    public void chooseCommandAction() {
        String commandName = this.parser.getCommandName();
        String[] args = this.parser.getArgs();
        this.commands.add(this.parser.getCommandLine());

        switch(commandName) {
            case "echo" -> this.echo(args);
            case "pwd" -> this.pwd();
            case "cd" -> this.cd(args);
            default -> {
                System.out.println(commandName + " is not a command.");
            }
        }            
    }
    
    public void launch() {
        Scanner in = new Scanner(System.in);
        String commandLine;
        boolean flag;
        
        System.out.println("Terminal Starts Here.");
        while(true) {
            do {
                System.out.print(">> ");
                commandLine = in.nextLine();
                flag = this.parser.parse(commandLine);
            } while(!flag);
            
            if (this.parser.getCommandName().equals("exit")) {
                break;
            }
            
            this.chooseCommandAction();   
        }
        in.close();
    }

//    echo
    public void echo(String[] args) {
        String sentence = String.join(" ", args);
        System.out.println(sentence);
    }
    
//    pwd
    public void pwd() {
        System.out.println(this.currPath.toString());
    }
    
//    cd
    public void cd(String[] args) {
        if (args.length == 0) {
            this.pwd();
            return;
        }
        
        String pathStr = args[0];
        Path newPath = this.currPath.resolve(pathStr);
        if (Files.exists(newPath)) {
            this.currPath = newPath.normalize();
        } else {
            System.out.println("The system cannot find the path specified.");
        }
    }
    
//    ls && ls -r
    public void ls(String[] args) {
        ArrayList<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(this.currPath)) {
            for (Path file : dirStream) {
                files.add(file);
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        
        if (files.isEmpty()) {
            System.out.println("Empty Directory!");
            return;
        }
        if (args.length > 0 && args[0].equals("-r")) {
            Collections.reverse(files);
        }
        
        for (Path file : files) {
            System.out.println(file.getFileName());
        }
    }
    
//    mkdir
    private void mkdirHelper(String folder) {
        File f1 = new File(folder);
        if (!f1.isAbsolute()) {
            f1 = new File(this.currPath.toAbsolutePath().toString() + "\\" + folder);
        }

        if (!f1.getParentFile().exists()) {
            System.out.println("Wrong directory"); return;
        }
        if (f1.exists()) {
            System.out.println("A subdirectory or file " + folder + " already exists"); return;
        }
        if (!f1.mkdirs()) {
            System.out.println("Cannot create directory");
        }
    }
    
    public void mkdir(String[] args) {
        if (args.length == 0) {
            System.out.println("The syntax of the command is incorrect.");
            return;
        }
        for (String arg : args) {
            this.mkdirHelper(arg);
        }
    }
    
//    rmdir
//    touch
//    cp
//    cp -r
//    rm
//    cat
//    wc
//    >
//    >>
//    history
//    exit
}
