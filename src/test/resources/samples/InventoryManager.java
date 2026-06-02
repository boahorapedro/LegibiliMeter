package br.ufpe.cin.metric.codeSamples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryManager {

    static List<String> products = new ArrayList<>();
    static List<Double> prices = new ArrayList<>();
    static List<Integer> quantities = new ArrayList<>();
    static List<Integer> minStock = new ArrayList<>();
    static List<String> notifSent = new ArrayList<>();
    static List<Boolean> active = new ArrayList<>();
    
    static Map<String, Long> lastNotifTime = new HashMap<>();

    public static void main(String[] args) {
        init();
        run();
    }

    public static void init() {
        addProduct("P001", 150.0, 5, 10);
        addProduct("P002", 89.5, 2, 8);
        addProduct("P003", 250.0, 0, 5);
        addProduct("P004", 45.0, 20, 15);
        addProduct("P005", 320.0, 3, 12);
    }

    public static void run() {
        int o = 0;
        while (o < 3) {
            checkLowStock();
            o++;
        }
        
        System.out.println("\n========== ESTOQUE ATUAL ==========");
        listProducts();
        
        System.out.println("\n========== OPERAÇÕES ==========");
        updateStock("P001", 5);
        updateStock("P002", -1);
        removeProduct("P003");
        
        System.out.println("\n========== RELATÓRIO FINAL ==========");
        listProducts();
    }

    public static void addProduct(String id, Double pr, Integer q, Integer ms) {
        products.add(id);
        prices.add(pr);
        quantities.add(q);
        minStock.add(ms);
        notifSent.add("N");
        active.add(true);
        lastNotifTime.put(id, System.currentTimeMillis());
    }

    public static void checkLowStock() {
        for (int i = 0; i < products.size(); i++) {
            if (quantities.get(i) < minStock.get(i) && active.get(i)) {
                System.out.println("🔔 ALERTA: " + products.get(i) + 
                    " acabando! Qtd: " + quantities.get(i) + 
                    " / Mín: " + minStock.get(i));
                
                notifSent.set(i, "Y");
                
            }
        }
    }

    public static void updateStock(String prodId, Integer qtd) {
        int idx = products.indexOf(prodId);
        if (idx != -1) {
            int q = quantities.get(idx);
            quantities.set(idx, q + qtd);
            System.out.println("✏️ " + prodId + " atualizado. Nova qtd: " + quantities.get(idx));
        } else {
            System.out.println("❌ Produto não encontrado");
        }
    }

    public static void removeProduct(String prodId) {
        int idx = products.indexOf(prodId);
        if (idx == -1) {
            System.out.println("Erro: produto nao existe");
            return;
        }
        
        active.set(idx, false);
        System.out.println("Produto " + prodId + " desativado");
        
    }

    public static void listProducts() {
        for (int i = 0; i < products.size(); i++) {
            String st = "✓";
            if (!active.get(i)) st = "✗";
            
            System.out.println(st + " [" + products.get(i) + "] " +
                "R$ " + prices.get(i) + 
                " | Qtd: " + quantities.get(i) + 
                " | Mín: " + minStock.get(i));
        }
    }

    public static void applyDiscount(String prodId, Double discPercent) {
        int idx = products.indexOf(prodId);
        if (idx != -1) {
            Double oldPrice = prices.get(idx);
            Double newPrice = oldPrice * (1.0 - discPercent / 100.0);
            prices.set(idx, newPrice);
            System.out.println("Desconto aplicado em " + prodId);
        }
    }

    public static void clearNotifications() {
        for (int i = 0; i < notifSent.size(); i++) {
            notifSent.set(i, "N");
        }
    }

}
