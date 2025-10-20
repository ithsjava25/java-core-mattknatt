package com.example;

import java.time.LocalDate;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class Warehouse {
    private static final Map<String, Warehouse> WAREHOUSES = new ConcurrentHashMap<>();

    private final String name;

    private final Map<UUID, Product> products;

    private final Set<UUID> changedProductIds;

    private Warehouse(String name) {
        this.name = name;
        this.products = new ConcurrentHashMap<>();
        this.changedProductIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    public static Warehouse getInstance(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Warehouse name must be provided");
        }
        return WAREHOUSES.computeIfAbsent(name, Warehouse::new);
    }

    public static Warehouse getInstance() {
        return getInstance("DefaultWarehouse");
    }

    public void addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }
        Product existingProduct = products.putIfAbsent(product.uuid(), product);
        if (existingProduct != null) {
            throw new IllegalArgumentException("Product with that id already exists, use updateProduct for updates.");
        }

        changedProductIds.add(product.uuid());

    }

    public List<Product> getProducts() {
        return products.values().stream().toList();
    }

    public Optional<Product> getProductById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(products.get(id));
    }

    public Map<Category, List<Product>> getProductsGroupedByCategories() {
        return products.values()
                .stream()
                .collect(Collectors.groupingBy(Product::category));

    }

    public void clearProducts() {
        Warehouse warehouse = WAREHOUSES.get(name);
        if (warehouse != null) {
            warehouse.products.clear();
            warehouse.changedProductIds.clear();
        }
    }

    public boolean isEmpty() {
        return products.isEmpty();
    }

    public void updateProductPrice(UUID id, BigDecimal newPrice) {
        Product product = getProductById(id).
                orElseThrow(() -> new NoSuchElementException("Product not found with id: " + id));
        product.setPrice(newPrice);
        changedProductIds.add(id);
    }

    public List<Product> getChangedProducts() {
        return changedProductIds.stream()
                .filter(products::containsKey)
                .map(products::get)
                .toList();
    }

    public List<Perishable> expiredProducts() {
        LocalDate today = LocalDate.now();
        return products.values().stream()
                .filter(p -> p instanceof Perishable)
                .filter(p -> ((Perishable) p).isExpired(today))
                .map(p -> (Perishable) p)
                .collect(Collectors.toList());

    }

    public List<Shippable> shippableProducts() {
        return products.values().stream()
                .filter(p -> p instanceof Shippable)
                .map(p -> (Shippable) p)
                .collect(Collectors.toList());
    }

    public void remove(UUID id) {
        if (id != null) {
            products.remove(id);
            changedProductIds.remove(id);
        }
    }
}
