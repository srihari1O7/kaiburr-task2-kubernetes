package com.kaiburr.task1.service;

import java.util.Set;
import java.util.stream.Stream;


public class ShellCommandValidator {

  
    private static final Set<String> DENIED_COMMANDS = Set.of(
        "rm", "curl", "wget", "mv", "cp", "sudo", "apt", "yum", "dnf",
        "del", "rmdir", "format", "git", "ssh", "scp", "netcat", "nc", "docker", "kubectl"
    );

   
    public static boolean isSafe(String command) {
        if (command == null || command.isBlank()) {
            return false;
        }

        
        String normalizedCommand = command.trim().toLowerCase();

        
        if (DENIED_COMMANDS.stream().anyMatch(denied -> normalizedCommand.startsWith(denied + " "))) {
            return false;
        }

       
        String[] metaChars = {";", "&&", "||", "|", "`", "$(", "<", ">"};
        if (Stream.of(metaChars).anyMatch(normalizedCommand::contains)) {
            return false;
        }
        
        
        return normalizedCommand.startsWith("echo ");
    }
}