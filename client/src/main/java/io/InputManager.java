package io;

import exceptions.EndOfInputException;
import exceptions.ScriptExecutionException;
import io.readers.ConsoleReader;
import io.readers.FileReader;
import io.readers.Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

public class InputManager {
    private static class ScriptSource {
        Reader reader;
        String fileName;

        ScriptSource(Reader reader, String fileName) {
            this.reader = reader;
            this.fileName = fileName;
        }
    }

    private final Logger logger = LoggerFactory.getLogger(InputManager.class);

    private final Set<String> pathHistory = new HashSet<>();

    private final Deque<ScriptSource> sourceDeque = new ArrayDeque<>();

    public void initReaders(Supplier<Set<String>> commandsSupplier) {
        try {
            sourceDeque.push(new ScriptSource(new ConsoleReader(commandsSupplier), null));
        } catch (IOException e) {
            logger.error("Не удалось открыть JLine консоль", e);
            System.out.println("Не удалось открыть консоль");
        }
    }

    public void enqueueScript(String fileName) throws IOException {
        if (pathHistory.contains(fileName)) {
            throw new ScriptExecutionException("Обнаружена рекурсия, файл: " + fileName);
        }
        sourceDeque.push(new ScriptSource(new FileReader(fileName), fileName));

        pathHistory.add(fileName);
        logger.info("В очередь добавлен новый скрипт {}", fileName);
    }

    public String readNextLine(String prompt, Set<String> suggestions, boolean isCommandMode) {
        Objects.requireNonNull(sourceDeque.peekFirst()).reader.setSuggestions(suggestions);
        Objects.requireNonNull(sourceDeque.peekFirst()).reader.setCommandMode(isCommandMode);

        while (!sourceDeque.isEmpty()) {
            ScriptSource currentSource = sourceDeque.peek();
            Reader currentReader = Objects.requireNonNull(currentSource).reader;
            if (currentReader.hasNextLine()) {
                String currentLine = currentReader.nextLine(prompt);
                if (currentLine != null) return currentLine;
            }
            if (sourceDeque.size() > 1) {
                currentReader.close();
                ScriptSource finishedSource = sourceDeque.pop();
                pathHistory.remove(finishedSource.fileName);

                logger.info("Скрипт {} завершен, возвращение к предыдущему источнику", finishedSource.fileName);
                System.out.println("Конец выполнения скрипта " + finishedSource.fileName);
                continue;
            }
            sourceDeque.pop();
            throw new EndOfInputException("Конец ввода");
        }
        throw new EndOfInputException("Чтение из пустой очереди");
    }

    public String readNextLine(String prompt, Set<String> suggestions) { // data
        return readNextLine(prompt, suggestions, false);
    }

    public String readNextLine(String prompt, boolean isCommandMode) { // commands
        return readNextLine(prompt, null, isCommandMode);
    }

    public boolean isScriptMode() {
        return sourceDeque.size() != 1;
    }
}