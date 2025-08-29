package com.benchmarking.dbcomparison.util;

import com.benchmarking.dbcomparison.model.*;
import com.github.javafaker.Faker;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class DataGenerator {
    private final Faker faker;
    private final Random random;

    public DataGenerator() {
        this.faker = new Faker(new Locale("pl"));
        this.random = new Random();
    }

    public Customer generateCustomer() {
        Customer customer = new Customer();
        customer.setFirstName(faker.name().firstName());
        customer.setLastName(faker.name().lastName());
        customer.setEmail(faker.internet().emailAddress(UUID.randomUUID().toString()));
        customer.setPhoneNumber(faker.phoneNumber().phoneNumber());
        customer.setDateOfBirth(LocalDate.now().minusYears(random.nextInt(18, 80)));
        customer.setAddressStreet(faker.address().streetAddress());
        customer.setAddressCity(faker.address().city());
        customer.setAddressPostalCode(faker.address().zipCode());
        customer.setAddressCountry("Polska");
        customer.setStatus("ACTIVE");
        customer.setLoyaltyPoints(random.nextInt(0, 1000));
        customer.setNewsletterSubscription(random.nextBoolean());
        customer.setLastLoginDate(LocalDateTime.now().minusDays(random.nextInt(30)));
        customer.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(365)));
        customer.setUpdatedAt(LocalDateTime.now());
        return customer;
    }

    public Brand generateBrand() {
        Brand brand = new Brand();
        brand.setName(faker.company().name());
        brand.setDescription(faker.company().catchPhrase());
        brand.setWebsite(faker.company().url());
        brand.setLogoUrl("https://example.com/" + faker.file().fileName());
        brand.setIsActive(true);  // Zmienione z setActive na setIsActive
        brand.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(365)));
        brand.setUpdatedAt(LocalDateTime.now());
        return brand;
    }

    public ProductCategory generateCategory(ProductCategory parentCategory) {
        ProductCategory category = new ProductCategory();
        category.setName(faker.commerce().department());
        category.setDescription(faker.lorem().sentence());
        category.setParentCategory(parentCategory);
        category.setLevel(parentCategory == null ? 1 : parentCategory.getLevel() + 1);
        category.setIsActive(true);  // Zmienione z setActive na setIsActive
        category.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(365)));
        category.setUpdatedAt(LocalDateTime.now());
        return category;
    }

    public Product generateProduct(Brand brand, ProductCategory category) {
        Product product = new Product();
        product.setName(truncate(faker.commerce().productName(), 255));
        product.setDescription(truncate(faker.lorem().paragraph(), 1000));
        product.setPrice(new BigDecimal(faker.commerce().price().replace(",", ".")));
        product.setStockQuantity(random.nextInt(0, 1000));
        product.setCategory(category);
        product.setBrand(brand);
        product.setWeight(BigDecimal.valueOf(random.nextDouble(0.1, 10.0)));
        product.setDimensions(truncate(random.nextInt(10, 100) + "x" + random.nextInt(10, 100) + "x" + random.nextInt(10, 100), 50));
        // Tu zmiana:
        String uniqueSku = "SKU-" + UUID.randomUUID().toString();
        product.setSku(uniqueSku);
        product.setBarcode(truncate(faker.code().ean13(), 50));
        product.setIsAvailable(true);
        product.setMinStockLevel(10);
        product.setMaxStockLevel(100);
        product.setRating(BigDecimal.valueOf(random.nextDouble(1.0, 5.0)));
        product.setReviewCount(random.nextInt(0, 100));
        product.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(365)));
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }


    public Order generateOrder(Customer customer, List<Product> availableProducts) {
        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderNumber(UUID.randomUUID());
        order.setStatus("NEW");
        order.setShippingAddressStreet(customer.getAddressStreet());
        order.setShippingAddressCity(customer.getAddressCity());
        order.setShippingAddressPostalCode(customer.getAddressPostalCode());
        order.setShippingAddressCountry(customer.getAddressCountry());
        order.setShippingMethod(faker.options().option("DPD", "InPost", "DHL", "FedEx"));
        order.setShippingCost(new BigDecimal("15.99"));
        order.setPaymentMethod(faker.options().option("BLIK", "CARD", "TRANSFER", "COD"));
        order.setPaymentStatus("PENDING");

        LocalDateTime orderDate = LocalDateTime.now().minusDays(random.nextInt(30));
        order.setOrderDate(orderDate);
        order.setEstimatedDeliveryDate(orderDate.toLocalDate().plusDays(3));
        order.setCreatedAt(orderDate);
        order.setUpdatedAt(orderDate);

        List<OrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        int numberOfItems = random.nextInt(1, 5);
        for (int i = 0; i < numberOfItems; i++) {
            Product randomProduct = availableProducts.get(random.nextInt(availableProducts.size()));
            OrderItem item = generateOrderItem(order, randomProduct);
            items.add(item);
            totalAmount = totalAmount.add(item.getTotalAmount());
        }

        order.setTotalAmount(totalAmount);
        order.setItems(items);  // Zmienione z setOrderItems na setItems
        return order;
    }


    public ProductReview generateReview(Product product, Customer customer) {
        ProductReview review = new ProductReview();
        review.setProduct(product);
        review.setCustomer(customer);
        review.setRating(random.nextInt(1, 6));
        review.setTitle(truncate(faker.lorem().sentence(3), 255));
        review.setComment(truncate(faker.lorem().paragraph(), 255));
        review.setIsVerified(false);
        review.setHelpfulVotes(random.nextInt(0, 50));
        review.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(30)));
        review.setUpdatedAt(LocalDateTime.now());
        return review;
    }

    public InventoryMovement generateInventoryMovement(Product product) {
        InventoryMovement movement = new InventoryMovement();
        movement.setProduct(product);
        movement.setMovementType(faker.options().option("RESTOCK", "SALE", "RETURN", "DAMAGE", "ADJUSTMENT"));
        movement.setQuantity(random.nextInt(-50, 101)); // Pozwala na ujemne wartości dla wyjść ze stanu
        movement.setReferenceType(faker.options().option("ORDER", "SUPPLIER_DELIVERY", "INVENTORY_CHECK", "RETURN_ORDER"));
        movement.setReferenceId(UUID.randomUUID());
        movement.setNotes(faker.lorem().sentence());
        movement.setCreatedAt(LocalDateTime.now().minusHours(random.nextInt(24 * 30))); // Ruchy w ciągu ostatnich 30 dni
        movement.setCreatedBy(UUID.randomUUID()); // Symulacja ID użytkownika
        return movement;
    }

    private OrderItem generateOrderItem(Order order, Product product) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(random.nextInt(1, 5));
        item.setUnitPrice(product.getPrice());

        BigDecimal baseAmount = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        BigDecimal discountRate = new BigDecimal("0.10");
        item.setDiscountAmount(baseAmount.multiply(discountRate).setScale(2, RoundingMode.HALF_UP));
        item.setTaxRate(new BigDecimal("0.23")); // 23% VAT
        BigDecimal taxable = baseAmount.subtract(item.getDiscountAmount());
        item.setTaxAmount(taxable.multiply(new BigDecimal("0.23")).setScale(2, RoundingMode.HALF_UP));
        item.setTotalAmount(taxable.add(item.getTaxAmount()).setScale(2, RoundingMode.HALF_UP));

        item.setCreatedAt(order.getCreatedAt());
        item.setUpdatedAt(order.getUpdatedAt());
        return item;
    }

    public Product generateProductWithKeyword(Brand brand, ProductCategory category, String keywordInName, String keywordInDescription) {
        Product product = new Product();

        // Wstawiamy wymuszony fragment do nazwy produktu z fakerem dla reszty nazwy
        String fakeName = faker.commerce().productName();
        // np. dopisz "Pro" na początek lub w środku nazwy
        product.setName(truncate(keywordInName + " " + fakeName, 255));

        // W opisie wymuszamy obecność słowa np. "wyjątkowy"
        String fakeDescription = faker.lorem().paragraph();
        product.setDescription(truncate(keywordInDescription + " " + fakeDescription, 1000));

        product.setPrice(new BigDecimal(faker.commerce().price().replace(",", ".")));
        product.setStockQuantity(random.nextInt(0, 1000));
        product.setCategory(category);
        product.setBrand(brand);
        product.setWeight(BigDecimal.valueOf(random.nextDouble(0.1, 10.0)));
        product.setDimensions(truncate(random.nextInt(10, 100) + "x" + random.nextInt(10, 100) + "x" + random.nextInt(10, 100), 50));
        String uniqueSku = "SKU-" + UUID.randomUUID().toString();
        product.setSku(uniqueSku);
        product.setBarcode(truncate(faker.code().ean13(), 50));
        product.setIsAvailable(true);
        product.setMinStockLevel(10);
        product.setMaxStockLevel(100);
        product.setRating(BigDecimal.valueOf(random.nextDouble(1.0, 5.0)));
        product.setReviewCount(random.nextInt(0, 100));
        product.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(365)));
        product.setUpdatedAt(LocalDateTime.now());

        return product;
    }

    public List<Product> generateMixedProducts(Brand brand, ProductCategory category, String keywordInName, String keywordInDescription, int totalCount, double keywordRatio) {
        List<Product> products = new ArrayList<>();
        int countWithKeyword = (int) (totalCount * keywordRatio);
        int countWithoutKeyword = totalCount - countWithKeyword;

        // Generuj produkty z keywordami
        for (int i = 0; i < countWithKeyword; i++) {
            products.add(generateProductWithKeyword(brand, category, keywordInName, keywordInDescription));
        }

        // Generuj produkty bez keywordów
        for (int i = 0; i < countWithoutKeyword; i++) {
            products.add(generateProduct(brand, category));
        }

        return products;
    }



    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
