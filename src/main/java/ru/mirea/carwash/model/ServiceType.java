package ru.mirea.carwash.model;

// типы услуг автомойки: у каждого есть название и базовая цена
public enum ServiceType {

    BODY_WASH("Мойка кузова", 500),
    COMPLEX("Комплексная мойка", 1200),
    DRY_CLEANING("Химчистка салона", 3000),
    POLISHING("Полировка кузова", 5000);

    private final String title;
    private final double basePrice;

    ServiceType(String title, double basePrice) {
        this.title = title;
        this.basePrice = basePrice;
    }

    public String getTitle() {
        return title;
    }

    public double getBasePrice() {
        return basePrice;
    }
}
