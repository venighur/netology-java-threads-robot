import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static final int ROUTES_COUNT = 1000;
    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();
    
    public static void main(String[] args) {
        for (int i = 0; i < ROUTES_COUNT; i++) {
            new Thread(() -> {
                String route = generateRoute("RLRFR", 100);
                int charCount = (int) route.chars().filter(ch -> ch == 'R').count();
                // критическая секция
                synchronized (sizeToFreq) {
                    if (sizeToFreq.containsKey(charCount)) {
                        sizeToFreq.put(charCount, sizeToFreq.get(charCount) + 1);
                    } else {
                        sizeToFreq.put(charCount, 1);
                    }
                    sizeToFreq.notify();
                }
                //
            }).start();
        }

        new Thread(() -> {
            // критическая секция
            synchronized (sizeToFreq) {
                if (sizeToFreq.values().stream().mapToInt(n -> n).sum() != ROUTES_COUNT) {
                    try {
                        sizeToFreq.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                int maxCount = Collections.max(sizeToFreq.values());
                StringBuilder maxRepeats = new StringBuilder();

                Set<Integer> keys = sizeToFreq
                    .entrySet()
                    .stream()
                    .filter(entry -> Objects.equals(entry.getValue(), maxCount))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());

                for (int i = 0; i < keys.size(); i++) {
                    int key = (int) keys.toArray()[i];
                    sizeToFreq.remove(key); // удаляем запись, чтобы не отображать в списке с другими замерами
                    maxRepeats.append(key);
                    if (i < keys.size() - 1) {
                        maxRepeats.append(", ");
                    }
                }

                System.out.println("Самое частое количество повторений " + maxRepeats + " (встретилось " + maxCount + " раз)");
                System.out.println("Другие размеры:");
                for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
                    System.out.println("- " + entry.getKey() + " (" + entry.getValue() + " раз)");
                }
            }
            //
        }).start();
    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }
}