import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gerenciador de Estoque refatorado.
 * Demonstra as melhorias aplicadas ao código legado.
 */
public class InventoryManagerRefactored {

    private final List<Product> products;
    private final NotificationService notificationService;

    public InventoryManagerRefactored() {
        this.products = new ArrayList<>();
        this.notificationService = new NotificationService();
    }

    public static void main(String[] args) {
        InventoryManagerRefactored manager = new InventoryManagerRefactored();
        manager.initializeSampleData();
        manager.demonstrateOperations();
    }

    private void initializeSampleData() {
        addProduct("P001", 150.0, 5, 10, 0);
        addProduct("P002", 89.5, 2, 8, 15);
        addProduct("P003", 250.0, 0, 5, 0);
        addProduct("P004", 45.0, 20, 15, 10);
        addProduct("P005", 320.0, 3, 12, 5);
    }

    private void demonstrateOperations() {
        System.out.println("\n========== VERIFICANDO ESTOQUE BAIXO ==========");
        checkLowStockMultipleTimes(3); // Chama 3 vezes para testar: deve ter UMA notificação cada
        
        System.out.println("\n========== ESTOQUE ATUAL ==========");
        displayAllProducts();
        
        System.out.println("\n========== OPERAÇÕES ==========");
        updateStockAndDisplay("P001", 5);
        updateStockAndDisplay("P002", -1);
        deactivateProduct("P003");
        
        System.out.println("\n========== ESTOQUE FINAL ==========");
        displayAllProducts();
    }

    private void addProduct(String id, double price, int quantity, 
                           int minStock, double discountPercentage) {
        products.add(new Product(id, price, quantity, minStock, discountPercentage));
        notificationService.initializeForProduct(id);
    }

    /**
     * Verifica estoque baixo SEM duplicar notificações.
     * Pode ser chamado múltiplas vezes sem prejudicar.
     */
    private void checkLowStockMultipleTimes(int times) {
        for (int i = 0; i < times; i++) {
            checkLowStock();
        }
    }

    private void checkLowStock() {
        for (Product product : products) {
            if (product.isLowStock() && product.isActive()) {
                // ✅ CORRIGIDO: Envia notificação apenas UMA vez por dia
                if (notificationService.shouldNotify(product.getId())) {
                    sendLowStockAlert(product);
                    notificationService.markNotificationSent(product.getId());
                }
            }
        }
    }

    private void sendLowStockAlert(Product product) {
        System.out.println("🔔 ALERTA: " + product.getId() + 
            " acabando! Qtd: " + product.getQuantity() + 
            " / Mín: " + product.getMinStock());
    }

    private void updateStockAndDisplay(String productId, int quantityChange) {
        Product product = findProductById(productId);
        if (product != null) {
            product.addQuantity(quantityChange);
            System.out.println("✏️ " + productId + " atualizado. Nova qtd: " + product.getQuantity());
        } else {
            System.out.println("❌ Produto não encontrado: " + productId);
        }
    }

    private void deactivateProduct(String productId) {
        Product product = findProductById(productId);
        if (product != null) {
            product.deactivate();
            System.out.println("✗ Produto " + productId + " desativado");
        } else {
            System.out.println("❌ Produto não encontrado: " + productId);
        }
    }

    private void displayAllProducts() {
        for (Product product : products) {
            System.out.println(product.getDisplayString());
        }
    }

    private Product findProductById(String id) {
        return products.stream()
            .filter(p -> p.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    // ============== CLASSES INTERNAS ============== //

    /**
     * Representa um produto no estoque.
     * Encapsula dados e comportamento de um produto.
     */
    private static class Product {
        private final String id;
        private final double basePrice;
        private int quantity;
        private final int minStock;
        private final double discountPercentage;
        private boolean active;

        public Product(String id, double basePrice, int quantity, 
                      int minStock, double discountPercentage) {
            this.id = id;
            this.basePrice = basePrice;
            this.quantity = quantity;
            this.minStock = minStock;
            this.discountPercentage = discountPercentage;
            this.active = true;
        }

        public String getId() { return id; }
        public int getQuantity() { return quantity; }
        public int getMinStock() { return minStock; }
        public boolean isActive() { return active; }

        public void addQuantity(int amount) {
            this.quantity += amount;
        }

        public void deactivate() {
            this.active = false;
        }

        public boolean isLowStock() {
            return quantity < minStock;
        }

        public double getFinalPrice() {
            return basePrice * (1.0 - discountPercentage / 100.0);
        }

        public String getDisplayString() {
            String status = active ? "✓" : "✗";
            String priceInfo = String.format("R$ %.2f", getFinalPrice());
            
            if (discountPercentage > 0) {
                priceInfo += String.format(" (desc. %.0f%% de R$ %.2f)", 
                    discountPercentage, basePrice);
            }
            
            return String.format("%s [%s] %s | Qtd: %d | Mín: %d", 
                status, id, priceInfo, quantity, minStock);
        }
    }

    /**
     * Gerencia notificações de estoque baixo.
     * Garante apenas UMA notificação por produto por dia.
     */
    private static class NotificationService {
        private final Map<String, LocalDate> lastNotificationDate = new HashMap<>();

        public void initializeForProduct(String productId) {
            lastNotificationDate.put(productId, null);
        }

        public boolean shouldNotify(String productId) {
            LocalDate lastDate = lastNotificationDate.getOrDefault(productId, null);
            LocalDate today = LocalDate.now();
            
            // Se nunca notificou ou a última foi em outro dia
            return lastDate == null || !lastDate.equals(today);
        }

        public void markNotificationSent(String productId) {
            lastNotificationDate.put(productId, LocalDate.now());
        }
    }
}
