{% extends 'base.exported.class' %}

{% block content %}
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import com.google.gson.Gson;
{% if is_test %}
import java.util.Arrays;
{% endif %}

class {{ class_name }} {

    private class Tree {
        private int[] lefts;
        private int[] rights;
        private double[] thresholds;
        private int[] indices;
        private int[][] classes;
    }
    private Tree tree;

    public {{ class_name }}(String file) throws FileNotFoundException {
        String jsonStr = new Scanner(new File(file)).useDelimiter("\\Z").next();
        this.tree = new Gson().fromJson(jsonStr, Tree.class);
    }

    private int findMax(int[] nums) {
        int idx = 0;
        for (int i = 0; i < nums.length; i++) {
            idx = nums[i] > nums[idx] ? i : idx;
        }
        return idx;
    }

    private double[] normVals(int[] nums) {
        int i = 0, l = nums.length;
        double[] result = new double[l];
        double sum = 0.;
        for (i = 0; i < l; i++) {
            sum += nums[i];
        }
        if(sum == 0) {
            for (i = 0; i < l; i++) {
                result[i] = 1.0 / nums.length;
            }
        } else {
            for (i = 0; i < l; i++) {
                result[i] = nums[i] / sum;
            }
        }
        return result;
    }

    private int predict(double[] features, int node) {
        if (this.tree.thresholds[node] != -2) {
            if (features[this.tree.indices[node]] <= this.tree.thresholds[node]) {
                return predict(features, this.tree.lefts[node]);
            } else {
                return predict(features, this.tree.rights[node]);
            }
        }
        return findMax(this.tree.classes[node]);
    }

    public int predict(double[] features) {
        return this.predict(features, 0);
    }

    private double[] predictProba(double[] features, int node) {
        if (this.tree.thresholds[node] != -2) {
            if (features[this.tree.indices[node]] <= this.tree.thresholds[node]) {
                return predictProba(features, this.tree.lefts[node]);
            } else {
                return predictProba(features, this.tree.rights[node]);
            }
        }
        return normVals(this.tree.classes[node]);
    }

    public double[] predictProba (double[] features) {
        return this.predictProba(features, 0);
    }

    public static void main(String[] args) throws FileNotFoundException {
        int nFeatures = {{ n_features }};
        if (args.length != (nFeatures + 1) || !args[0].endsWith(".json")) {
            throw new IllegalArgumentException("You have to pass the path to the exported model data and " +  String.valueOf(nFeatures) + " features.");
        }

        // Features:
        double[] features = new double[args.length-1];
        for (int i = 1, l = args.length; i < l; i++) {
            features[i - 1] = Double.parseDouble(args[i]);
        }

        // Model data:
        String modelData = args[0];

        // Estimator:
        {{ class_name }} clf = new {{ class_name }}(modelData);

        // Get class prediction:
        int prediction = clf.predict(features);
        {% if not is_test %}
        System.out.println("Predicted class: #" + String.valueOf(prediction));
        {% endif %}

        // Get class probabilities:
        double[] probabilities = clf.predictProba(features);
        {% if not is_test %}
        for (int i = 0; i < probabilities.length; i++) {
            System.out.print(String.valueOf(probabilities[i]));
            if (i != probabilities.length - 1) {
                System.out.print(",");
            }
        }
        {% endif %}

        {% if is_test %}
        System.out.println("{\"predict\": " + String.valueOf(prediction) + ", \"predict_proba\": " + String.join(",", Arrays.toString(probabilities)) + "}");
        {% endif %}
    }
}
{% endblock %}