package app.cli;

public class Parser {
    private String commandName;
    private String commandLine;
    private String[] args;
    
    public boolean parse(String input) {
        String[] tokens = input.trim().split("\\s+");
        if (tokens.length == 0) {
            return false;
        }
        
        this.commandLine = input;
        this.commandName = tokens[0];
        this.args = new String[tokens.length - 1];
        System.arraycopy(tokens, 1, this.args, 0, this.args.length);
        return true;
    }

    public String getCommandName() {
        return commandName;
    }

    public String getCommandLine() {
        return commandLine;
    }

    public String[] getArgs() {
        return args;
    }
}
