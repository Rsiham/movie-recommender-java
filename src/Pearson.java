
/*
 * Nom : Rafai Siham
 * Numéro d'étudiant : 25235658
 * Cours : INF6104
 * Travail noté 6 - Filtrage collaboratif
 */

import java.io.*;
import java.util.*;

public class Pearson {

    static Map<String, Map<Integer, Double>> data = new LinkedHashMap<>();

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage : java Pearson evaluations.txt facteur");
            return;
        }

        lireFichier(args[0]);
        double amplification = Double.parseDouble(args[1]);

        double mae = calculerMAE(amplification);
        System.out.printf("MAE Pearson = %.4f%n", mae);
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

    static double calculerMAE(double amplification) {
        double sommeErreur = 0.0;
        int total = 0;

        for (String user : data.keySet()) {
            for (Integer item : new ArrayList<>(data.get(user).keySet())) {
                double vraieNote = data.get(user).get(item);

                data.get(user).remove(item);
                double prediction = predire(user, item, amplification);
                data.get(user).put(item, vraieNote);

                sommeErreur += Math.abs(vraieNote - prediction);
                total++;
            }
        }

        return sommeErreur / total;
    }

    static double predire(String user, int item, double amplification) {
        double moyenneUser = moyenne(data.get(user));
        double numerateur = 0.0;
        double denominateur = 0.0;

        for (String autre : data.keySet()) {
            if (autre.equals(user)) continue;
            if (!data.get(autre).containsKey(item)) continue;

            double sim = similaritePearson(user, autre);

            if (sim == 0) continue;

            sim = Math.signum(sim) * Math.pow(Math.abs(sim), amplification);

            double moyenneAutre = moyenne(data.get(autre));
            numerateur += sim * (data.get(autre).get(item) - moyenneAutre);
            denominateur += Math.abs(sim);
        }

        if (denominateur == 0) {
            return moyenneUser;
        }

        double prediction = moyenneUser + numerateur / denominateur;
        return limiter(prediction);
    }

    static double similaritePearson(String u1, String u2) {
        Map<Integer, Double> r1 = data.get(u1);
        Map<Integer, Double> r2 = data.get(u2);

        double m1 = moyenne(r1);
        double m2 = moyenne(r2);

        double num = 0.0;
        double den1 = 0.0;
        double den2 = 0.0;

        for (Integer item : r1.keySet()) {
            if (r2.containsKey(item)) {
                double a = r1.get(item) - m1;
                double b = r2.get(item) - m2;

                num += a * b;
                den1 += a * a;
                den2 += b * b;
            }
        }

        if (den1 == 0 || den2 == 0) return 0.0;

        return num / (Math.sqrt(den1) * Math.sqrt(den2));
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
}
