package ru.vorobev.server.chat.auth;

import java.sql.*;

public class DbAuthService implements IAuthService {

    private static final String DB_URL = "jdbc:sqlite:chatUsers.db";
    private Connection connection;
    private PreparedStatement getUsernameStatement;
    private PreparedStatement updateUsernameStatement;

    @Override
    public void start() {
        try {
            System.out.println("Создание соедененеия к базе данных");
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("Успешное соединение к базе данных");
            getUsernameStatement = createGetUsernameStatement();
            updateUsernameStatement = createUpdateUsernameStatement();
        } catch (SQLException e) {
            System.err.println("Ошибка подключения к базе данных ");
            e.printStackTrace();
        }
    }

    @Override
    public String getUserNameByLoginAndPassword(String login, String password) {
        String username = null;
        try {
            getUsernameStatement.setString(1, login);
            getUsernameStatement.setString(2, password);
            ResultSet resultSet = getUsernameStatement.executeQuery();
            while (resultSet.next()) {
                username = resultSet.getString("username");
                break;
            }
            resultSet.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            System.err.printf("Не удалось получить имя из базы данных. Login: %s; password: %s%n", login, password);
        }

        return username;
    }

    @Override
    public void stop() {
        IAuthService.super.stop();
    }

    @Override
    public void updateUsername(String currentUsername, String newUsername) {
        try {
            updateUsernameStatement.setString(1, newUsername);
            updateUsernameStatement.setString(2, currentUsername);
            int result = updateUsernameStatement.executeUpdate();
            System.out.println("Обновление имя пользователя. Обновленные строки: " + result);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Не удалось обновить имя пользователя: " + currentUsername);
        }
    }

    private PreparedStatement createGetUsernameStatement() throws SQLException {
        return connection.prepareStatement("SELECT username FROM users WHERE login = ? AND password = ? ");
    }

    private PreparedStatement createUpdateUsernameStatement() throws SQLException {
        return connection.prepareStatement("UPDATE users SET username = ? WHERE username = ? ");
    }
}
