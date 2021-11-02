import java.util.*;

public class Main {
    public static void main(String[] args) {
        // write your code here
        Scanner scanner = new Scanner(System.in);
        List<Integer> numbers = new ArrayList<>();
        int n = 0;
        while (scanner.hasNext()) {
            numbers.add(scanner.nextInt());
        }

        n = numbers.get(numbers.size() - 1);
        numbers.remove(numbers.size() - 1);

        List<Integer> distances = new ArrayList<>();
        int min = Integer.MAX_VALUE;

        for (Integer number: numbers) {
            distances.add(Math.abs(number - n));
        }

        //find minimum distances

        for (Integer number: distances) {
            if (number < min) {
                min = number;
            }
        }


        List<Integer> indexes = new ArrayList<>();
// identify minimum indexes
        for (int i = 0; i < distances.size(); i++) {
            if (min == distances.get(i)) {
                indexes.add(i);
            }
        }

        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < indexes.size(); i++) {
            result.add(numbers.get(indexes.get(i)));
        }



        //order ascending
        for (int i = 0; i < result.size() - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < result.size(); j++) {

                if (result.get(minIndex) > result.get(j)) {
                    minIndex = j;
                }
            }
            Integer aux = result.get(i);
            result.set(i, result.get(minIndex));
            result.set(minIndex, aux);
        }

        for (Integer number: result) {
            System.out.printf("%d ", number);
        }

    }
}
