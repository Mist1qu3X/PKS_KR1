package carwash;

import carwash.ui.ConsoleApp;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.NoSuchElementException;

public class Main {

    public static void main(String[] args) {
        // включаем UTF-8, иначе кириллица в консоли выводится криво
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // UTF-8 поддерживается всегда
        }
        try {
            new ConsoleApp().run();
        } catch (NoSuchElementException e) {
            // ввод закончился (например, поток закрыли) — выходим без ошибки
        }
    }
}
