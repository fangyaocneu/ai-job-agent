package com.fangyao.agent;

import java.sql.Connection;

public class DatabaseTest {

    public static void main(String[] args) {

        try (
                Connection connection =
                        DatabaseConfig.getConnection()
        ) {

            System.out.println(
                    "Connected to PostgreSQL successfully!"
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}