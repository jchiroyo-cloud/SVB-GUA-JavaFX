
package com.svb.gua;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class MapaAsientos extends VBox {

    private final GridPane grid = new GridPane();

    private final Set<String> seleccionados =
            new LinkedHashSet<>();

    private final Set<String> ocupados =
            new LinkedHashSet<>();

    private final Label titulo =
            new Label("SELECCIÓN DE ASIENTOS");

    private final Label estado =
            new Label("Selecciona tus asientos.");

    private int cantidadActual = 80;
    private int maxSeleccionados = 1;

    public MapaAsientos() {

        setSpacing(12);
        setPadding(new Insets(15));
        setAlignment(Pos.CENTER);

        titulo.setStyle(
                "-fx-font-size:20px;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:#252536;"
        );

        Label escenario = new Label("ESCENARIO");

        escenario.setMaxWidth(Double.MAX_VALUE);
        escenario.setAlignment(Pos.CENTER);

        escenario.setStyle(
                "-fx-background-color:#252536;" +
                "-fx-text-fill:white;" +
                "-fx-padding:15px;" +
                "-fx-font-weight:bold;" +
                "-fx-background-radius:8px;"
        );

        grid.setHgap(7);
        grid.setVgap(7);
        grid.setAlignment(Pos.CENTER);

        estado.setStyle(
                "-fx-font-size:13px;" +
                "-fx-font-weight:bold;"
        );

        getChildren().addAll(
                titulo,
                escenario,
                grid,
                estado
        );

        generarAsientos(80);
    }

    public void generarAsientos(int cantidad) {

        if (cantidad < 1) {
            cantidad = 1;
        }

        cantidadActual = cantidad;

        seleccionados.clear();

        dibujarAsientos();
    }

    private void dibujarAsientos() {

        grid.getChildren().clear();

        int columnas = 10;

        for (int i = 0; i < cantidadActual; i++) {

            int fila = i / columnas;
            int columna = i % columnas;

            String codigo =
                    letraFila(fila) + (columna + 1);

            Button asiento = new Button(codigo);

            asiento.setPrefSize(52, 38);

            actualizarColor(asiento, codigo);

            asiento.setOnAction(e -> {

                if (ocupados.contains(codigo)) {
                    return;
                }

                if (seleccionados.contains(codigo)) {

                    seleccionados.remove(codigo);

                } else {

                    if (seleccionados.size()
                            >= maxSeleccionados) {

                        estado.setText(
                                "Solo puedes seleccionar " +
                                maxSeleccionados +
                                " asiento(s)."
                        );

                        return;
                    }

                    seleccionados.add(codigo);
                }

                actualizarColor(asiento, codigo);

                actualizarEstado();
            });

            grid.add(asiento, columna, fila);
        }

        actualizarEstado();
    }

    private String letraFila(int numero) {

        StringBuilder resultado =
                new StringBuilder();

        int n = numero + 1;

        while (n > 0) {

            n--;

            resultado.insert(
                    0,
                    (char) ('A' + n % 26)
            );

            n /= 26;
        }

        return resultado.toString();
    }

    private void actualizarColor(
            Button boton,
            String codigo) {

        if (ocupados.contains(codigo)) {

            boton.setStyle(
                    "-fx-background-color:#e74c3c;" +
                    "-fx-text-fill:white;" +
                    "-fx-font-weight:bold;"
            );

            boton.setDisable(true);

        } else if (seleccionados.contains(codigo)) {

            boton.setStyle(
                    "-fx-background-color:#3498db;" +
                    "-fx-text-fill:white;" +
                    "-fx-font-weight:bold;"
            );

            boton.setDisable(false);

        } else {

            boton.setStyle(
                    "-fx-background-color:#2ecc71;" +
                    "-fx-text-fill:white;" +
                    "-fx-font-weight:bold;"
            );

            boton.setDisable(false);
        }
    }

    private void actualizarEstado() {

        estado.setText(
                "Asientos seleccionados: " +
                seleccionados.size() +
                " / " +
                maxSeleccionados
        );
    }

    public void setMaxSeleccionados(int cantidad) {

        maxSeleccionados = Math.max(1, cantidad);

        if (seleccionados.size() > maxSeleccionados) {
            seleccionados.clear();
        }

        dibujarAsientos();
    }

    public Set<String> getAsientosSeleccionados() {

        return new LinkedHashSet<>(seleccionados);
    }

    public void establecerAsientosOcupados(
            Collection<String> lista) {

        ocupados.clear();

        if (lista != null) {
            ocupados.addAll(lista);
        }

        seleccionados.clear();

        dibujarAsientos();
    }

    public void limpiarSeleccion() {

        seleccionados.clear();

        dibujarAsientos();
    }

    public int getCantidadSeleccionados() {

        return seleccionados.size();
    }
}
