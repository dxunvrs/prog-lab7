package multithread;

import java.sql.Connection;

public class PooledConnection implements AutoCloseable {
    private final Connection connection;
    private final DBConnectionPool pool;

    public PooledConnection(Connection connection, DBConnectionPool pool) {
        this.connection = connection;
        this.pool = pool;
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public void close() {
        pool.releaseConnection(connection);
    }
}
