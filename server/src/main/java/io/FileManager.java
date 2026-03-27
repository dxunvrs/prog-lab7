package io;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvReadException;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import core.CollectionManager;
import models.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class FileManager {
    private static final Logger logger = LoggerFactory.getLogger(FileManager.class);

    private final CsvMapper mapper = new CsvMapper();
    private final CsvSchema schema = mapper.schemaFor(Product.class);

    private String fileName = "collection.csv";

    public FileManager() {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void load(CollectionManager collectionManager) {
        try (Scanner scanner = new Scanner(new File(fileName))) {
            LocalDateTime dateOfInit = null;
            if (scanner.hasNextLine()) {
                dateOfInit = LocalDateTime.parse(scanner.nextLine());
            }

            while (scanner.hasNextLine()) {
                String dateLine = scanner.nextLine();
                if (dateLine.trim().isEmpty()) continue;
                Product product = mapper.readerFor(Product.class).with(schema).readValue(dateLine);
                collectionManager.addProduct(product);
            }

            collectionManager.initCollection(dateOfInit);
            logger.info("Коллекция из файла {} успешно загружена", fileName);
            System.out.println("Коллекция из файла " + fileName + " успешно загружена");

        } catch (DateTimeParseException e) {
            logger.error("Не удалось распарсить дату", e);
            System.out.println("Неверный формат даты инициализации коллекции в файле, загрузка не удалась, создана новая коллекция");

        } catch (CsvReadException | JsonParseException e) {
            logger.error("Не удалось распарсить файл", e);
            System.out.println("Структура CSV не распознана, загрузка не удалась, создана новая коллекция");

        } catch (InvalidFormatException e) {
            logger.error("Не удалось распарсить данный тип", e);
            System.out.println("Неверный формат данных, загрузка не удалась, создана новая коллекция");

        } catch (DatabindException e) {
            logger.error("Ошибка маппинга полей", e);
            System.out.println("Ошибка маппинга полей, загрузка не удалась, создана новая коллекция");

        } catch (IOException e) {
            logger.error("Ошибка загрузки", e);
            System.out.println("Ошибка загрузки: " + e.getMessage());
            System.out.println("Создана новая коллекция");
        }
    }

    public void save(CollectionManager collectionManager) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(collectionManager.getDateOfInit().toString());
            writer.newLine();

            collectionManager.saveCollection(i -> {
                try {
                    String line = mapper.writer(schema.withoutHeader()).writeValueAsString(i).trim();
                    writer.write(line);
                    writer.newLine();
                } catch (IOException e) {
                    System.out.println("Ошибка сохранения: " + e.getMessage());
                }
            });
            logger.info("Коллекция успешно сохранена в файл: {}", fileName);
            System.out.println("Коллекция успешно сохранена в файл");
        } catch (IOException e) {
            logger.error("Ошибка сохранения", e);
            System.out.println("Ошибка сохранения: " + e.getMessage());
        }
    }
}
