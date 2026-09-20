package ru.mirea.carwash.model;

import java.time.LocalDateTime;

// клиент автомойки
public class Client {

    private int id;
    private String fullName;
    private String phone;
    private String email;
    private LocalDateTime createdAt;

    // новый клиент (id присвоит база)
    public Client(String fullName, String phone, String email) {
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
    }

    // клиент, прочитанный из базы
    public Client(int id, String fullName, String phone, String email, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("Клиент #%d | %s | тел: %s | email: %s",
                id, fullName, phone, (email == null || email.isEmpty() ? "-" : email));
    }
}
