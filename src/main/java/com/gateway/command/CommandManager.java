package com.gateway.command;


import com.chat.util.json.JsonObjectFactory;
import com.chat.util.json.JsonProtocol;
import com.gateway.server.ServerDataEvent;
import com.gateway.socket.ConnectionProperties;
import com.gateway.socket.SenderSocketHandler;
import com.gateway.socket.ZmqContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandManager {
    private static Pattern pattern = Pattern.compile("([a-zA-Z]+)(:\\d+){0,2}");
    private static String DEFAULT_REPLY = "";
    private static Logger logger = LoggerFactory.getLogger(CommandManager.class);
    private SenderSocketHandler sender = new SenderSocketHandler();


    private Map<String, Command> commandMap = new ConcurrentHashMap<String, Command>() {
        {
            put(Command.DATABASE, request -> {
                try (ZMQ.Socket handler = ZmqContextHolder.getContext().socket(ZMQ.SUB)) {
                    Properties properties = ConnectionProperties.getProperties();
                    handler.connect(properties.getProperty("from_butler_address"));
                    handler.subscribe(Command.DATABASE.getBytes());

                    sender.send(request);
                    logger.debug("Send to database {}", request);
                    System.out.println(handler.recvStr());

                    String reply = handler.recvStr();
                    logger.debug("Receive from database {}", reply);
                    JsonProtocol protocol = JsonObjectFactory.getObjectFromJson(reply, JsonProtocol.class);
                    handler.unsubscribe(Command.DATABASE.getBytes());
                    return JsonObjectFactory.getJsonString(Optional.ofNullable(protocol).orElse(new JsonProtocol()));
                }
            });
            put(Command.CHAT, request -> {
                sender.send(request);
                return DEFAULT_REPLY;
            });

            put(Command.ROOM_MANAGER, request -> {
                try (ZMQ.Socket handler = ZmqContextHolder.getContext().socket(ZMQ.SUB)) {
                    Properties properties = ConnectionProperties.getProperties();
                    handler.connect(properties.getProperty("from_butler_address"));
                    sender.send(request);
                    handler.subscribe(Command.ROOM_MANAGER.getBytes());
                    logger.debug("Send to RoomManager {}", request);
                    System.out.println(handler.recvStr());
                    String reply = handler.recvStr();
                    logger.debug("Receive from RoomManager {}", reply);
                    handler.unsubscribe(Command.ROOM_MANAGER.getBytes());
                    return JsonObjectFactory.getObjectFromJson(reply, JsonProtocol.class).getFrom();
                }
            });
        }
    };

    public String execute(ServerDataEvent dataEvent) {
        String json = new String(dataEvent.getData());
        JsonProtocol request = JsonObjectFactory.getObjectFromJson(json, JsonProtocol.class);
        Optional<JsonProtocol> protocolOptional = Optional.ofNullable(request);
        String keyTo = protocolOptional.map(JsonProtocol::getTo).orElse(DEFAULT_REPLY);
        Command command = commandMap.getOrDefault(getServiceName(keyTo), r -> Command.NO_COMMAND);

        return command.execute(json);
    }

    private String getServiceName(String keyTo) {
        Matcher matcher = pattern.matcher(keyTo);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "";
    }
}
