package app.cli;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
            case "ls" -> this.ls(args);
            case "mkdir" -> this.mkdir(args);
            case "rmdir" -> this.rmdir(args);
            case "touch" -> this.touch(args);
            case "cat" -> this.cat(args);
            case "wc" -> this.wc(args);
            case "history" -> this.history();
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

    public void echo(String[] args) {
        String sentence = String.join(" ", args);
        System.out.println(sentence);
    }
    
    public void pwd() {
        System.out.println(this.currPath.toString());
    }
    
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
    
    private void rmdirHelper() {
        File dir = new File(this.currPath.toAbsolutePath().toString());
        File[] files = dir.listFiles();
        if (files == null) {
            System.out.println("There is no empty directories"); return;
        }

        for (int i = 0; i < files.length; ++i) {
            if (files[i].isDirectory() && files[i].list().length == 0) {
                files[i].delete();
            }
        }
    }
    
    public void rmdir(String[] args) {
        if (args.length == 0) {
            System.out.println("The syntax of the command is incorrect"); return;
        }

        if (args[0].equals("*")) {
            rmdirHelper();
        }
        else {
            File f1 = new File(args[0]);
            if (!f1.isAbsolute()) {
                f1 = new File(this.currPath.toAbsolutePath().toString() + "\\" + args[0]);
            }
            if (!f1.exists()) {
                System.out.println("The system cannot find the file specified."); return;
            }
            if (!f1.isDirectory()) {
                System.out.println("The directory name is invalid."); return;
            }
            if (!f1.delete()) {
                System.out.println("The directory is not empty.");
            }
        }
    }
    
    public void touch(String[] args) {
        if (args.length == 0) {
            System.out.println("no file names given");
            return;
        }
        
        for (String arg : args) {
            Path file = this.currPath.resolve(arg);
            try {
                Files.createFile(file);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
    
//    cp
//    cp -r
//    rm
    
    private void catHelper(String fileName) {
        Path filePath = this.currPath.resolve(fileName);
        if (!Files.exists(filePath)) {
            System.out.println("cat: " + fileName + ": No such file or directory."); return;
        }
        
        File f1 = new File(filePath.toString());
        if (!f1.isFile()) {
            System.out.println("cat: " + fileName + ": No such a file"); return;
        }
        try (Scanner sc = new Scanner(f1)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    
    public void cat(String[] args) {
        if (args.length == 0) {
            System.out.println("No arguments were provided"); return;
        }
        int size = args.length <= 2 ? args.length : 2;
        for (int i = 0; i < size; ++i) {
            catHelper(args[i]);
        }
    }
    
    public void wc(String[] args) {
        if (args.length == 0) {
            System.out.println("No arguments were provided"); return;
        }
        
        Path filePath = this.currPath.resolve(args[0]);
        if (!Files.exists(filePath)) {
            System.out.println("wc: " + args[0] + ": No such file or directory"); return;
        }
        
        File fl = new File(filePath.toString());
        if (!fl.isFile()) {
            System.out.println("wc: " + args[0] + ": No such file"); return;            
        }
        
        int linesCount = 0, wordCount = 0, charCount = 0;
        try (Scanner sc = new Scanner(fl)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                linesCount += 1;
                wordCount += line.split("\\s+").length;
                charCount += line.toCharArray().length;
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        System.out.println(linesCount + " " + wordCount + " " + charCount + " " + args[0]);
    }
    
//    >
//    >>

    public void history() {
        for (int i = 0; i < this.commands.size(); ++i) {
            System.out.println(i+1 + " " + this.commands.get(i));
        }
    }
}
