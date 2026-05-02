package commands;

import models.Product;

import java.util.ArrayList;
import java.util.List;

public class CommandData {
    private final String message;
    private List<Product> products = new ArrayList<>();

    public CommandData(String message) {
        this.message = message;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public String message() {
        return message;
    }

    public List<Product> products() {
        return products;
    }
}