import java.util.ArrayList;
import java.util.List;

public class OrderProcessing {

    public static void main(String[] args) {
        process();
    }

    public static void process() {
        // "c" = customer??? não sabemos
        String c = "BRONZE";
        
        // "it" = items, "p" = price?, "q" = quantity?
        List<Double> it = new ArrayList<>();
        it.add(100.0);
        it.add(50.0);
        it.add(75.0);
        
        // Cálculo do total com péssima nomenclatura
        Double v1 = 0.0;
        for (Double x : it) {
            v1 = v1 + x;
        }
        
        // tp = tipo? type? Type of what???
        Integer tp = 0;
        if (c.equals("BRONZE")) {
            tp = 1;
        } else {
            if (c.equals("SILVER")) {
                tp = 2;
            } else {
                if (c.equals("GOLD")) {
                    tp = 3;
                } else {
                    if (c.equals("PLATINUM")) {
                        tp = 4;
                    } else {
                        tp = 0;
                    }
                }
            }
        }
        
        // Descrição de desconto? Qual é o 0.9? Qual é o 0.95???
        Double v2 = v1;
        if (tp == 1) {
            v2 = v1 * 0.9;
        } else {
            if (tp == 2) {
                v2 = v1 * 0.85;
            } else {
                if (tp == 3) {
                    v2 = v1 * 0.80;
                } else {
                    if (tp == 4) {
                        v2 = v1 * 0.75;
                    } else {
                        v2 = v1;
                    }
                }
            }
        }
        
        // rg = region? "SP", "MG", "BA", "SC"?? Mágico!
        String rg = "SP";
        
        // f = freight? Números mágicos soltos!!!
        Double f = 0.0;
        if (rg.equals("SP")) {
            f = 15.0;
        } else {
            if (rg.equals("MG")) {
                f = 20.0;
            } else {
                if (rg.equals("BA")) {
                    f = 35.0;
                } else {
                    if (rg.equals("SC")) {
                        f = 25.0;
                    } else {
                        f = 50.0;
                    }
                }
            }
        }
        
        // final = total???
        Double final_total = v2 + f;
        
        // Falta de tratamento de erro, falta de logging, falta de validação
        // Se adicionar um novo tipo de cliente ou região, é pura tortura!
        Boolean flag = true;
        if (final_total > 0) {
            if (flag) {
                System.out.println("Pedido processado");
                System.out.println("Subtotal: " + v1);
                System.out.println("Com desconto: " + v2);
                System.out.println("Frete: " + f);
                System.out.println("TOTAL: " + final_total);
            }
        } else {
            System.out.println("Erro no processamento");
        }
    }
}
