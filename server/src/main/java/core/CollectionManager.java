package core;

import exceptions.CommandExecutionException;
import exceptions.IdNotFoundException;
import models.Product;
import network.DBManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CollectionManager {
    private static final Logger logger = LoggerFactory.getLogger(CollectionManager.class);

    private List<Product> collection;
    private LocalDateTime dateOfInit = LocalDateTime.now();

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

    public void addProduct(Product product) {
        Product newProduct = dbManager.createProduct(product);
        if (newProduct == null) throw new CommandExecutionException("Не удалось добавить продукт");
        collection.add(newProduct);
        logger.info("В коллекцию добавлен новый продукт {}", newProduct);
    }

    public void removeProductById(int id) {
        checkId(id);

        if (!dbManager.deleteProduct(id)) throw new CommandExecutionException("Не удалось удалить продукт");
        collection.removeIf(product -> product.getId() == id);

        logger.info("Из коллекции удален элемент с id {}", id);
    }

    public void updateProductById(int id, Product newProduct) {
        checkId(id);

        if (!dbManager.updateProduct(id, newProduct)) throw new CommandExecutionException("Не удалось обновить продукт");

        Product updatedProduct = collection.stream()
                .filter(product -> product.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IdNotFoundException("Нет такого id"));

        updatedProduct.update(newProduct);
        logger.info("Элемент с id {} обновлен, новое значение {}", id, newProduct);
    }

    private void checkId(int id) {
        if (collection.stream().noneMatch(product -> product.getId() == id)) throw new IdNotFoundException("Нет такого id");
    }

    public void clearCollection() {
        if (!dbManager.clearProducts()) throw new CommandExecutionException("Не удалось очистить коллекцию");

        collection.clear();
        logger.info("Коллекция очищена");
    }

    public LocalDateTime getDateOfInit() {
        return dateOfInit;
    }

    public int getSumOfPrice() {
        return collection.stream().mapToInt(Product::getPrice).sum();
    }

    public double getAvgOfPrice() {
        return collection.stream().mapToInt(Product::getPrice).average().orElse(0.0);
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
                  Дата инициализации: %s
                  Количество элементов: %s""".formatted(collection.getClass().getSimpleName(),
                dateOfInit.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), getCollectionSize());
    }

    public int getCollectionSize() {
        return collection.size();
    }

    public void saveCollection(Consumer<Product> saveAction) {
        collection.forEach(saveAction);
    }
}
