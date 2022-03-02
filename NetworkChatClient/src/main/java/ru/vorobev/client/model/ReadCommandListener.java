package ru.vorobev.client.model;

import ru.vorobev.clientserver.Command;

public interface ReadCommandListener {

    void processReceivedCommand(Command command);

}
