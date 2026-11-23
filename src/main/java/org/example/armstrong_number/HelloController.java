package org.example.armstrong_number;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class HelloController {
    @FXML private TextField upperLimitField;
    @FXML private TextField threadCountField;
    @FXML private Button startButton;
    @FXML private Button stopButton;
    @FXML private Label errorLabel;
    @FXML private Label statusLabel;
    @FXML private FlowPane workerPane;
    @FXML private TextArea resultsArea;
    @FXML private Label armstrongCountLabel;
    @FXML private Label nonArmstrongCountLabel;
    @FXML private Label totalCheckedLabel;

    private final List<SearchWorker> workers = new ArrayList<>();
    private int finished;

    @FXML
    private void initialize() {
        errorLabel.managedProperty().bind(errorLabel.visibleProperty());
    }

    @FXML
    private void startSearch() {
        if (!workers.isEmpty()) {
            return;
        }
        int limit;
        int count;
        try {
            limit = positiveInteger(upperLimitField.getText(), "Upper limit");
            count = positiveInteger(threadCountField.getText(), "Thread count");
            if (count > Math.min(limit, 64)) {
                throw new IllegalArgumentException("Thread count cannot exceed " + Math.min(limit, 64) + ".");
            }
        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
            errorLabel.setVisible(true);
            return;
        }

        errorLabel.setVisible(false);
        resultsArea.clear();
        showCounts(0, 0);
        workerPane.getChildren().clear();
        finished = 0;
        setRunning(true);
        for (ArmstrongSearch.Range range : ArmstrongSearch.splitRanges(limit, count)) {
            SearchWorker worker = new SearchWorker(range);
            workers.add(worker);
            addWorkerTile(worker, workers.size());
        }
        showProgress("Searching.");
        for (int i = 0; i < workers.size(); i++) {
            Thread thread = new Thread(workers.get(i), "armstrong-worker-" + (i + 1));
            thread.setDaemon(true);
            thread.start();
        }
    }

    private static int positiveInteger(String text, String field) {
        try {
            int value = Integer.parseInt(text.trim());
            if (value > 0) {
                return value;
            }
        } catch (NumberFormatException ignored) {
            // Malformed and oversized values share the same field error.
        }
        throw new IllegalArgumentException(field + " must be a whole number from 1 to 2147483647.");
    }

    private void addWorkerTile(SearchWorker worker, int number) {
        ArmstrongSearch.Range range = worker.range;
        ProgressIndicator progress = new ProgressIndicator(0);
        progress.setPrefSize(48, 48);
        progress.progressProperty().bind(worker.progressProperty());
        Label bounds = new Label(range.start() + " to " + range.end());
        bounds.setWrapText(true);
        bounds.setMaxWidth(Double.MAX_VALUE);
        Label size = new Label(range.size() + (range.size() == 1 ? " number" : " numbers"));
        Label state = new Label("Ready");
        VBox tile = new VBox(6, new Label("Thread " + number), progress, bounds, size, state);
        tile.setAlignment(Pos.TOP_CENTER);
        tile.setPrefWidth(150);
        workerPane.getChildren().add(tile);

        worker.setOnRunning(event -> state.setText("Running"));
        worker.setOnSucceeded(event -> {
            if (workers.contains(worker)) {
                state.setText("Done");
                finished++;
                if (finished == workers.size()) {
                    publishResults();
                } else {
                    showProgress("Searching.");
                }
            }
        });
        worker.setOnFailed(event -> {
            if (workers.contains(worker)) {
                clearRun("Search failed.");
            }
        });
    }

    private void publishResults() {
        StringJoiner output = new StringJoiner(", ");
        int matchCount = 0;
        int checked = 0;
        for (SearchWorker worker : workers) {
            for (int match : worker.getValue()) {
                output.add(Integer.toString(match));
                matchCount++;
            }
            checked += worker.range.size();
        }
        resultsArea.setText(output.toString());
        showCounts(matchCount, checked);
        showProgress("Complete. Checked all " + checked + " numbers from 1 to " + checked + ".");
        workers.clear();
        setRunning(false);
    }

    private void showProgress(String status) {
        statusLabel.setText(status + " Finished threads: " + finished + " of " + workers.size() + ".");
    }

    private void showCounts(int matches, int checked) {
        armstrongCountLabel.setText("Armstrong numbers: " + matches);
        nonArmstrongCountLabel.setText("Non-Armstrong numbers: " + (checked - matches));
        totalCheckedLabel.setText("Total checked: " + checked);
    }

    private void setRunning(boolean running) {
        upperLimitField.setDisable(running);
        threadCountField.setDisable(running);
        startButton.setDisable(running);
        stopButton.setDisable(!running);
    }

    @FXML
    private void stopSearch() {
        clearRun("Stopped.");
    }

    private void clearRun(String status) {
        cancelWorkers();
        workerPane.getChildren().clear();
        resultsArea.clear();
        showCounts(0, 0);
        finished = 0;
        showProgress(status);
        setRunning(false);
    }

    void cancelWorkers() {
        for (SearchWorker worker : workers) {
            worker.cancel(true);
        }
        workers.clear();
    }

    private static final class SearchWorker extends Task<List<Integer>> {
        private final ArmstrongSearch.Range range;

        SearchWorker(ArmstrongSearch.Range range) {
            this.range = range;
            updateProgress(0, range.size());
        }

        @Override
        protected List<Integer> call() {
            List<Integer> matches = new ArrayList<>();
            int size = range.size();
            int batchSize = Math.max(1, size / 500);
            int checked = 0;
            int batch = 0;
            for (long n = range.start(); n <= range.end(); n++) {
                if (isCancelled()) {
                    break;
                }
                if (ArmstrongSearch.isArmstrong((int) n)) {
                    matches.add((int) n);
                }
                checked++;
                if (++batch == batchSize || checked == size) {
                    updateProgress(checked, size);
                    batch = 0;
                }
            }
            return matches;
        }
    }
}
