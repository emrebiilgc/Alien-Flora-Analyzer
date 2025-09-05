import java.io.File;

public class Main {
    public static void main(String[] args) {
        if (args.length == 1) {
            File file = new File(args[0]);
            AlienFlora flora = new AlienFlora(file);

            flora.readGenomes();
            flora.evaluateEvolutions();
            flora.evaluateAdaptations();
        } else {
            System.out.println("wrong format or xml file");
        }
    }
}
