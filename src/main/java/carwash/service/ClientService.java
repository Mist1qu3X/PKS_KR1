package carwash.service;

import carwash.exception.BusinessException;
import carwash.exception.EntityNotFoundException;
import carwash.model.Client;
import carwash.repository.ClientRepository;

import java.util.List;

// логика работы с клиентами: проверки + обращение к репозиторию
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Client create(String fullName, String phone, String email) {
        validateName(fullName);
        validatePhone(phone);
        validateEmail(email);
        if (clientRepository.findByPhone(phone.trim()).isPresent()) {
            throw new BusinessException("Клиент с телефоном " + phone.trim() + " уже существует.");
        }
        Client client = new Client(fullName.trim(), phone.trim(), normalizeEmail(email));
        return clientRepository.save(client);
    }

    public List<Client> getAll() {
        return clientRepository.findAll();
    }

    public Client getById(int id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Клиент с ID " + id + " не найден."));
    }

    public void update(int id, String fullName, String phone, String email) {
        Client client = getById(id);
        validateName(fullName);
        validatePhone(phone);
        validateEmail(email);

        // телефон не должен быть занят другим клиентом
        clientRepository.findByPhone(phone.trim()).ifPresent(existing -> {
            if (existing.getId() != id) {
                throw new BusinessException("Телефон " + phone.trim() + " занят другим клиентом.");
            }
        });

        client.setFullName(fullName.trim());
        client.setPhone(phone.trim());
        client.setEmail(normalizeEmail(email));
        clientRepository.update(client);
    }

    public void delete(int id) {
        getById(id);
        clientRepository.deleteById(id);
    }

    // ---------- проверки ----------

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("Имя клиента обязательно.");
        }
        String trimmed = name.trim();
        if (trimmed.length() < 2 || trimmed.length() > 100) {
            throw new BusinessException("Имя клиента должно содержать от 2 до 100 символов.");
        }
        // только буквы, пробел, точка и дефис — поэтому "1" или "123" не пройдут
        if (!trimmed.matches("[\\p{L}][\\p{L} .\\-]*")) {
            throw new BusinessException(
                    "Имя может содержать только буквы, пробелы и дефис (например: Иван Петров).");
        }
    }

    private void validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new BusinessException("Телефон клиента обязателен.");
        }
        String trimmed = phone.trim();
        if (!trimmed.matches("[0-9 +()\\-]+")) {
            throw new BusinessException(
                    "Телефон может содержать только цифры и символы + - ( ). Пример: +7-900-123-45-67.");
        }
        long digits = trimmed.chars().filter(Character::isDigit).count();
        if (digits < 6 || digits > 15) {
            throw new BusinessException("Телефон должен содержать от 6 до 15 цифр.");
        }
    }

    private void validateEmail(String email) {
        // email необязателен
        if (email == null || email.trim().isEmpty()) {
            return;
        }
        if (!email.trim().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new BusinessException("Некорректный email. Пример: user@mail.ru.");
        }
    }

    // пустой email сохраняем как null
    private String normalizeEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        return email.trim();
    }
}
