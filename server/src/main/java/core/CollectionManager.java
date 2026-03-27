package core;

import exceptions.IdNotFoundException;
import models.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CollectionManager {
    private static final Logger logger = LoggerFactory.getLogger(CollectionManager.class);

    private final List<Product> collection = new LinkedList<>();
    private LocalDateTime dateOfInit = LocalDateTime.now();

    private int lastId = 0;

    public void initCollection(LocalDateTime dateOfInit) {
        this.dateOfInit = dateOfInit;
        lastId = collection.stream().mapToInt(Product::getId).max().orElse(0);
        logger.info("Инициализация коллекции, последний id: {}, дата: {}", lastId, dateOfInit);
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
        product.setId(++lastId);
        product.setCreationDate(new Date());
        collection.add(product);
        logger.info("В коллекцию добавлен новый продукт {}", product);
    }

    public void removeProductById(int id) {
        boolean removed = collection.removeIf(product -> product.getId()==id);
        if (!removed) throw new IdNotFoundException("Нет такого id");
        logger.info("Из коллекции удален элемент с id {}", id);
    }

    public void updateProductById(int id, Product newProduct) {
        Product updatedProduct = collection.stream()
                .filter(product -> product.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IdNotFoundException("Нет такого id"));
        updatedProduct.update(newProduct);
        logger.info("Элемент с id {} обновлен, новое значение {}", id, newProduct);
    }

    public void clearCollection() {
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
