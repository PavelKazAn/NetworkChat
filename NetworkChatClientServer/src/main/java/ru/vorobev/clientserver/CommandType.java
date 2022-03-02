package ru.vorobev.clientserver;

public enum CommandType {
    AUTH,
    AUTH_OK,
    ERROR,
    PUBLIC_MESSAGE,
    PRIVATE_MESSAGE,
    CLIENT_MESSAGE,
    END,
    UPDATE_USERNAME,
    UPDATE_USER_LIST
}