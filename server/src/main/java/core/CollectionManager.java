package core;

import exceptions.CommandExecutionException;
import exceptions.IdNotFoundException;
import models.Product;
import network.DBManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CollectionManager {
    private static final Logger logger = LoggerFactory.getLogger(CollectionManager.class);

    private List<Product> collection;

    private final DBManager dbManager;

    public CollectionManager(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    public void initCollection() {
        collection = dbManager.loadCollection();
        logger.info("Коллекция проинициализирована");
    }

    public void sort() {
        Collections.sort(collection);
        logger.info("Коллекция отсортирована в естественном порядке");
    }

    public void randomSort() {
        Collections.shuffle(collection);
        logger.info("Коллекция отсортирована в случайном порядке");
    }

    public void addProduct(Product product, int userId) {
        Product newProduct = dbManager.createProduct(product, userId);
        if (newProduct == null) throw new CommandExecutionException("Не удалось добавить продукт");
        collection.add(newProduct);
        logger.info("В коллекцию добавлен новый продукт {}", newProduct);
    }

    public void removeProductById(int id, int userId) {
        if (!dbManager.deleteProduct(id, userId)) throw new IdNotFoundException("Нет такого id");
        collection.removeIf(product -> product.getId() == id && product.getUserId() == userId);

        logger.info("Из коллекции удален элемент с id {}", id);
    }

    public void updateProductById(int id, Product newProduct, int userId) {
        if (!dbManager.updateProduct(id, newProduct, userId)) throw new IdNotFoundException("Нет такого id");

        Product updatedProduct = collection.stream()
                .filter(product -> product.getId() == id && product.getUserId() == userId)
                .findFirst()
                .orElseThrow(() -> new IdNotFoundException("Нет такого id"));

        updatedProduct.update(newProduct);
        logger.info("Элемент с id {} обновлен, новое значение {}", id, newProduct);
    }

    public void clearCollection(int userId) {
        if (!dbManager.clearProducts(userId)) throw new CommandExecutionException("Не удалось очистить коллекцию");

        collection.clear();
        logger.info("Коллекция очищена");
    }

    public int getSumOfPrice(int userId) {
        return collection.stream().filter(product -> product.getUserId() == userId).mapToInt(Product::getPrice).sum();
    }

    public double getAvgOfPrice(int userId) {
        return collection.stream().filter(product -> product.getUserId() == userId).mapToInt(Product::getPrice).average().orElse(0.0);
    }

    public String getFormattedCollection(Predicate<Product> filter) {
        if (collection.isEmpty()) return "Коллекция пуста";

        String result = collection.stream()
                .filter(filter).map(Product::toFormattedString).collect(Collectors.joining("\n"));

        if (result.isEmpty()) return "Совпадений не найдено";
        return result;
    }

    public String getCollectionInfo() {
        return """
                Информация о коллекции:
                  Тип: %s
                  Количество элементов: %s""".formatted(collection.getClass().getSimpleName(), collection.size());
    }
}
