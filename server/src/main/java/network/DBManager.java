package network;

import exceptions.InvalidAuthorizeException;
import models.Coordinates;
import models.Person;
import models.Product;
import models.UnitOfMeasure;
import multithread.DBConnectionPool;
import multithread.PooledConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

public class DBManager {
    private static final Logger logger = LoggerFactory.getLogger(DBManager.class);

    private final DBConnectionPool pool;

    public DBManager(DBConnectionPool pool) {
        this.pool = pool;
    }

    public List<Product> loadCollection() {
        try (PooledConnection pooledConnection = pool.getConnection()) {
            Connection connection = pooledConnection.getConnection();

            String sql = "SELECT * FROM products";
            List<Product> collection = new LinkedList<>();
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Product product = mapRowToProduct(resultSet);
                    collection.add(product);
                }
            } catch (SQLException e) {
                logger.error("Ошибка БД при загрузке коллекции", e);
            }
            return collection;
        } catch (InterruptedException e) {
            logger.error("Ошибка получения соединения", e);
            return new LinkedList<>();
        }
    }

    // возвращаем product с назначенными id, creationDate и userId
    public Product createProduct(Product product, int userId) {
        try (PooledConnection pooledConnection = pool.getConnection()) {
            Connection connection = pooledConnection.getConnection();

            String sql = "INSERT INTO products (name, x, y, price, unit_of_measure, owner_name, owner_birthday, owner_height, user_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id, creation_date";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, product.getName());
                statement.setLong(2, product.getCoordinates().x());
                statement.setInt(3, product.getCoordinates().y());
                statement.setInt(4, product.getPrice());
                statement.setObject(5, product.getUnitOfMeasure(), Types.OTHER);
                statement.setString(6, product.getOwner().name());
                statement.setDate(7, Date.valueOf(product.getOwner().birthday()));
                statement.setLong(8, product.getOwner().height());

                statement.setInt(9, userId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        product.setId(resultSet.getInt("id"));
                        product.setCreationDate(resultSet.getDate("creation_date"));
                        product.setUserId(userId);
                    }
                    logger.info("В БД добавлен продукт {}", product);
                    return product;
                }
            } catch (SQLException e) {
                logger.error("Ошибка БД при создании продукта", e);
            }
        } catch (InterruptedException e) {
            logger.error("Ошибка получения соединения", e);
        }
        return null;
    }

    public boolean updateProduct(int id, Product product, int userId) {
        try (PooledConnection pooledConnection = pool.getConnection()) {
            Connection connection = pooledConnection.getConnection();

            String sql = "UPDATE products SET name = ?, x = ?, y = ?, price = ?, unit_of_measure = ?, owner_name = ?, owner_birthday = ?, owner_height = ? " +
                    "WHERE id = ? AND user_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, product.getName());
                statement.setLong(2, product.getCoordinates().x());
                statement.setInt(3, product.getCoordinates().y());
                statement.setInt(4, product.getPrice());
                statement.setObject(5, product.getUnitOfMeasure(), Types.OTHER);
                statement.setString(6, product.getOwner().name());
                statement.setDate(7, Date.valueOf(product.getOwner().birthday()));
                statement.setLong(8, product.getOwner().height());

                statement.setInt(9, id);
                statement.setInt(10, userId);

                if (statement.executeUpdate() > 0) {
                    logger.info("Продукт с id {} обновлен в БД", id);
                    return true;
                }
            } catch (SQLException e) {
                logger.error("Ошибка БД при обновлении продукта", e);
            }
        } catch (InterruptedException e) {
            logger.error("Ошибка получения соединения", e);
        }
        return false;
    }

    public boolean deleteProduct(int id, int userId) {
        try (PooledConnection pooledConnection = pool.getConnection()) {
            Connection connection = pooledConnection.getConnection();

            String sql = "DELETE FROM products WHERE id = ? AND user_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                statement.setInt(2, userId);

                if (statement.executeUpdate() > 0) {
                    logger.info("Продукт с id {} удален из БД", id);
                    return true;
                }
            } catch (SQLException e) {
                logger.error("Ошибка БД при удалении продукта", e);
            }
        } catch (InterruptedException e) {
            logger.error("Ошибка получения соединения", e);
        }
        return false;
    }

    public boolean clearProducts(int userId) {
        try (PooledConnection pooledConnection = pool.getConnection()) {
            Connection connection = pooledConnection.getConnection();

            String sql = "DELETE FROM products WHERE user_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, userId);

                if (statement.executeUpdate() > 0) {
                    logger.info("Продукты удалены");
                    return true;
                }
            } catch (SQLException e) {
                logger.error("Ошибка БД при удалении продукта", e);
            }
        } catch (InterruptedException e) {
            logger.error("Ошибка получения соединения", e);
        }
        return false;
    }

    private Product mapRowToProduct(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String name = resultSet.getString("name");
        long x = resultSet.getLong("x");
        int y = resultSet.getInt("y");
        Date creationDate = resultSet.getDate("creation_date");
        int price = resultSet.getInt("price");
        String unitOfMeasureString = resultSet.getString("unit_of_measure");
        UnitOfMeasure unitOfMeasure = UnitOfMeasure.valueOf(unitOfMeasureString);
        String ownerName = resultSet.getString("owner_name");
        LocalDate ownerBirthday = resultSet.getDate("owner_birthday")
                .toLocalDate();
        long ownerHeight = resultSet.getLong("owner_height");

        int userId = resultSet.getInt("user_id");
        Product product = new Product(
                id, name, new Coordinates(x, y), creationDate, price,
                unitOfMeasure, new Person(ownerName, ownerBirthday, ownerHeight)
        );
        product.setUserId(userId);

        return product;
    }

    // возвращает id зарегистрированного пользователя
    public int registerUser(String username, String hash) {
        try (PooledConnection pooledConnection = pool.getConnection()) {
            Connection connection = pooledConnection.getConnection();

            String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?) RETURNING id";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                statement.setString(2, hash);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt("id");
                    }
                }
            } catch (SQLException e) {
                logger.error("Ошибка БД при добавлении пользователя", e);
            }
        } catch (InterruptedException e) {
            logger.error("Ошибка получения соединения", e);
        }
        throw new InvalidAuthorizeException("Данное имя уже занято");
    }

    public String getUserHash(String username) {
        try (PooledConnection pooledConnection = pool.getConnection()) {
            Connection connection = pooledConnection.getConnection();

            String sql = "SELECT password_hash FROM users WHERE username = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString("password_hash");
                    }
                }
            } catch (SQLException e) {
                logger.error("Ошибка БД при авторизации пользователя", e);
            }
        } catch (InterruptedException e) {
            logger.error("Ошибка получения соединения", e);
        }
        throw new InvalidAuthorizeException("Такого пользователя не существует");
    }

    public int getUserId(String username) {
        try (PooledConnection pooledConnection = pool.getConnection()) {
            Connection connection = pooledConnection.getConnection();

            String sql = "SELECT id FROM users WHERE username = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt("id");
                    }
                }
            } catch (SQLException e) {
                logger.error("Ошибка БД при поиске пользователя", e);
            }
        } catch (InterruptedException e) {
            logger.error("Ошибка получения соединения", e);
        }
        throw new InvalidAuthorizeException("Такого пользователя не существует");
    }
}
