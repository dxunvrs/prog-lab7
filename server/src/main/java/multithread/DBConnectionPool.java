package multithread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DBConnectionPool implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(DBConnectionPool.class);

    private final BlockingQueue<Connection> pool;
    private final List<Connection> allConnections;

    public DBConnectionPool(String host, int port, String db, String user, String pass) throws SQLException {
        this.pool = new LinkedBlockingQueue<>(10);
        this.allConnections = new ArrayList<>(10);

        String url = String.format("jdbc:postgresql://%s:%d/%s", host, port, db);
        for (int i = 0; i < 10; i++) {
            Connection connection = DriverManager.getConnection(url, user, pass);
            pool.add(connection);
            allConnections.add(connection);
        }
        logger.debug("Создан пул из {} соединений для БД", allConnections.size());
    }

    public PooledConnection getConnection() throws InterruptedException {
        return new PooledConnection(pool.take(), this);
    }

    public void releaseConnection(Connection connection) {
        boolean added = pool.offer(connection);
        if (!added) {
            logger.warn("Очередь соединений к БД переполнена");
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("Ошибка при закрытии соединения");
            }
        }
    }

    @Override
    public void close() {
        logger.debug("Закрытие пула соединений для БД");
        for (Connection connection: allConnections) {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                logger.error("Ошибка при закрытии соединения", e);
            }
        }
    }
}
