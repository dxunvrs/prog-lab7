package core;

import exceptions.CommandExecutionException;
import exceptions.DBExecuteException;
import exceptions.IdNotFoundException;
import models.Product;
import db.DBManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CollectionManager {
    private static final Logger logger = LoggerFactory.getLogger(CollectionManager.class);

    private final ReentrantLock lock = new ReentrantLock();

    private List<Product> collection;

    private final DBManager dbManager;

    public CollectionManager(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    public void initCollection() {
        lock.lock();
        try {
            collection = dbManager.loadCollection();
            logger.info("Коллекция проинициализирована");
        } finally {
            lock.unlock();
        }
    }

    public void sort() {
        lock.lock();
        try {
            Collections.sort(collection);
            logger.info("Коллекция отсортирована в естественном порядке");
        } finally {
            lock.unlock();
        }
    }

    public void randomSort() {
        lock.lock();
        try {
            Collections.shuffle(collection);
            logger.info("Коллекция отсортирована в случайном порядке");
        } finally {
            lock.unlock();
        }
    }

    public void addProduct(Product product, int userId) {
        Product newProduct = dbManager.createProduct(product, userId);
        if (newProduct == null) throw new CommandExecutionException("Не удалось добавить продукт");

        lock.lock();
        try {
            collection.add(newProduct);
            logger.info("В коллекцию добавлен новый продукт {}", newProduct);
        } finally {
            lock.unlock();
        }
    }

    public void removeProductById(int id, int userId) {
        try {
            dbManager.deleteProduct(id, userId);
        } catch (DBExecuteException e) {
            throw new CommandExecutionException(e.getMessage());
        }

        lock.lock();
        try {
            collection.removeIf(product -> product.getId() == id && product.getUserId() == userId);
            logger.info("Из коллекции удален элемент с id {}", id);
        } finally {
            lock.unlock();
        }
    }

    public void updateProductById(int id, Product newProduct, int userId) {
        try {
            dbManager.updateProduct(id, newProduct, userId);
        } catch (DBExecuteException e) {
            throw new CommandExecutionException(e.getMessage());
        }

        lock.lock();
        try {
            Product updatedProduct = collection.stream()
                    .filter(product -> product.getId() == id && product.getUserId() == userId)
                    .findFirst()
                    .orElseThrow(() -> new IdNotFoundException("Нет такого id"));
            updatedProduct.update(newProduct);
            logger.info("Элемент с id {} обновлен, новое значение {}", id, newProduct);
        } finally {
            lock.unlock();
        }
    }

    public void clearCollection(int userId) {
        if (!dbManager.clearProducts(userId)) throw new CommandExecutionException("Не удалось очистить коллекцию");

        lock.lock();
        try {
            collection.clear();
            logger.info("Коллекция очищена");
        } finally {
            lock.unlock();
        }
    }

    public int getSumOfPrice(int userId) {
        lock.lock();
        try {
            return collection.stream().filter(product -> product.getUserId() == userId).mapToInt(Product::getPrice).sum();
        } finally {
            lock.unlock();
        }
    }

    public double getAvgOfPrice(int userId) {
        lock.lock();
        try {
            return collection.stream().filter(product -> product.getUserId() == userId).mapToInt(Product::getPrice).average().orElse(0.0);
        } finally {
            lock.unlock();
        }
    }

    public String getFormattedCollection(Predicate<Product> filter) {
        lock.lock();
        try {
            if (collection.isEmpty()) return "Коллекция пуста";

            String result = collection.stream()
                    .filter(filter).map(Product::toFormattedString).collect(Collectors.joining("\n"));

            if (result.isEmpty()) return "Совпадений не найдено";
            return result;
        } finally {
            lock.unlock();
        }
    }

    public String getCollectionInfo() {
        lock.lock();
        try {
            return """
                    Информация о коллекции:
                      Тип: %s
                      Количество элементов: %s""".formatted(collection.getClass().getSimpleName(), collection.size());
        } finally {
            lock.unlock();
        }
    }
}
