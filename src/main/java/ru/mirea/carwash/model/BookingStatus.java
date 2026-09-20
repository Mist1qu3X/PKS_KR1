package ru.mirea.carwash.model;

// статусы записи и правила переходов между ними
public enum BookingStatus {

    CREATED("Создана"),
    CONFIRMED("Подтверждена"),
    IN_PROGRESS("В работе"),
    COMPLETED("Завершена"),
    CANCELLED("Отменена");

    private final String title;

    BookingStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    // можно ли сменить текущий статус на target
    public boolean canChangeTo(BookingStatus target) {
        switch (this) {
            case CREATED:
                return target == CONFIRMED || target == CANCELLED;
            case CONFIRMED:
                return target == IN_PROGRESS || target == CANCELLED;
            case IN_PROGRESS:
                return target == COMPLETED || target == CANCELLED;
            case COMPLETED:
            case CANCELLED:
            default:
                return false; // завершённую и отменённую менять нельзя
        }
    }

    public boolean isFinal() {
        return this == COMPLETED || this == CANCELLED;
    }
}
