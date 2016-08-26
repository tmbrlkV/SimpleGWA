package com.gateway.command;

interface Command {
    String CHAT = "chat";
    String NO_COMMAND = "";
    String DATABASE = "database";
    String ROOM_MANAGER = "roomManager";

    String execute(String request);
}
