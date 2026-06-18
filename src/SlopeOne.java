/*
 * Nom : Rafai Siham
 * Numéro d'étudiant : 25235658
 * Cours : INF6104
 * Travail noté 6 - Filtrage collaboratif
 */
import java.io.*;
import java.util.*;

public class SlopeOne {

    static Map<String, Map<Integer, Double>> data = new LinkedHashMap<>();

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage : java SlopeOne evaluations.txt");
            return;
        }

        lireFichier(args[0]);

        double mae = calculerMAE();
        System.out.printf("MAE Slope One = %.4f%n", mae);
    }

    static void lireFichier(String fichier) throws Exception {
        Evaluations evaluations = new Evaluations(fichier);

        for (String user : evaluations.utilisateurs()) {
            Map<Integer, Double> notes = new HashMap<>();

            for (Map.Entry<String, Double> entry : evaluations.evaluations(user)) {
                notes.put(Integer.parseInt(entry.getKey()), entry.getValue());
            }

            data.put(user, notes);
        }
    }

    static double calculerMAE() {
        double sommeErreur = 0.0;
        int total = 0;

        for (String user : data.keySet()) {
            for (Integer item : new ArrayList<>(data.get(user).keySet())) {
                double vraieNote = data.get(user).get(item);

                data.get(user).remove(item);
                double prediction = predire(user, item);
                data.get(user).put(item, vraieNote);

                sommeErreur += Math.abs(vraieNote - prediction);
                total++;
            }
        }

        return sommeErreur / total;
    }

    static double predire(String user, int itemCible) {
        Map<Integer, Double> notesUser = data.get(user);

        double numerateur = 0.0;
        double denominateur = 0.0;

        for (Integer itemConnu : notesUser.keySet()) {
            Difference diff = calculerDifference(itemCible, itemConnu);

            if (diff.count > 0) {
                numerateur += (notesUser.get(itemConnu) + diff.valeur) * diff.count;
                denominateur += diff.count;
            }
        }

        if (denominateur == 0) {
            return moyenne(notesUser);
        }

        double prediction = numerateur / denominateur;
        return limiter(prediction);
    }

    static Difference calculerDifference(int itemA, int itemB) {
        double somme = 0.0;
        int count = 0;

        for (String user : data.keySet()) {
            Map<Integer, Double> notes = data.get(user);

            if (notes.containsKey(itemA) && notes.containsKey(itemB)) {
                somme += notes.get(itemA) - notes.get(itemB);
                count++;
            }
        }

        if (count == 0) return new Difference(0.0, 0);

        return new Difference(somme / count, count);
    }

    static double moyenne(Map<Integer, Double> notes) {
        if (notes.isEmpty()) return moyenneGlobale();

        double somme = 0.0;
        for (double v : notes.values()) somme += v;
        return somme / notes.size();
    }

    static double moyenneGlobale() {
        double somme = 0.0;
        int total = 0;

        for (Map<Integer, Double> notes : data.values()) {
            for (double v : notes.values()) {
                somme += v;
                total++;
            }
        }

        return somme / total;
    }

    static double limiter(double note) {
        if (note < 1) return 1;
        if (note > 8) return 8;
        return note;
    }

    static class Difference {
        double valeur;
        int count;

        Difference(double valeur, int count) {
            this.valeur = valeur;
            this.count = count;
        }
    }
}
