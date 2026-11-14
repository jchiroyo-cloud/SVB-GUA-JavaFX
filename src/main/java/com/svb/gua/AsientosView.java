
package com.svb.gua;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class AsientosView extends VBox {

    private final GridPane mapa = new GridPane();

    private final Set<String> asientosSeleccionados =
            new LinkedHashSet<>();

    private final Set<String> asientosOcupados =
            new LinkedHashSet<>();

    private final int FILAS = 8;
    private final int COLUMNAS = 10;

    public AsientosView() {

        setSpacing(18);
        setPadding(new Insets(25));
        setAlignment(Pos.TOP_CENTER);

        setStyle(
            "-fx-background-color: #f5f6fa;" +
            "-fx-background-radius: 15;"
        );

        Label titulo = new Label("SELECCIÓN DE ASIENTOS");
        titulo.setStyle(
            "-fx-font-size: 23px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #20243a;"
        );

        Label escenario = new Label("ESCENARIO");
        escenario.setAlignment(Pos.CENTER);
        escenario.setMaxWidth(Double.MAX_VALUE);
        escenario.setPadding(new Insets(15));
        escenario.setStyle(
            "-fx-background-color: #252b45;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 17px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 10;"
        );

        mapa.setAlignment(Pos.CENTER);
        mapa.setHgap(9);
        mapa.setVgap(9);
        mapa.setPadding(new Insets(10));

        crearMapa();

        HBox leyenda = crearLeyenda();

        Label seleccion = new Label();
        seleccion.setId("resumenAsientos");
        seleccion.setText("Asientos seleccionados: 0");
        seleccion.setStyle(
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #252b45;"
        );

        getChildren().addAll(
            titulo,
            escenario,
            mapa,
            leyenda,
            seleccion
        );
    }

    private void crearMapa() {

        mapa.getChildren().clear();

        String[] letras = {
            "A", "B", "C", "D",
            "E", "F", "G", "H"
        };

        for (int fila = 0; fila < FILAS; fila++) {

            for (int columna = 0; columna < COLUMNAS; columna++) {

                String numero = letras[fila] + (columna + 1);

                Button asiento = new Button(
                    String.valueOf(columna + 1)
                );

                asiento.setPrefSize(43, 38);
                asiento.setMinSize(43, 38);
                asiento.setMaxSize(43, 38);

                asiento.setUserData(numero);

                if (asientosOcupados.contains(numero)) {

                    asiento.setDisable(true);
                    asiento.setStyle(estiloOcupado());

                } else {

                    asiento.setStyle(estiloDisponible());

                    asiento.setOnAction(event -> {

                        if (asientosSeleccionados.contains(numero)) {

                            asientosSeleccionados.remove(numero);
                            asiento.setStyle(estiloDisponible());

                        } else {

                            asientosSeleccionados.add(numero);
                            asiento.setStyle(estiloSeleccionado());
                        }

                        actualizarResumen();
                    });
                }

                Label etiquetaFila = new Label(
                    letras[fila]
                );

                etiquetaFila.setStyle(
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 13px;"
                );

                if (columna == 0) {
                    mapa.add(etiquetaFila, 0, fila);
                }

                mapa.add(asiento, columna + 1, fila);
            }
        }
    }

    private HBox crearLeyenda() {

        HBox leyenda = new HBox(20);
        leyenda.setAlignment(Pos.CENTER);

        leyenda.getChildren().addAll(
            crearIndicador("Disponible", "#22a06b"),
            crearIndicador("Ocupado", "#d94343"),
            crearIndicador("Seleccionado", "#2878e5")
        );

        return leyenda;
    }

    private HBox crearIndicador(String texto, String color) {

        Label cuadro = new Label(" ");
        cuadro.setPrefSize(17, 17);
        cuadro.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-background-radius: 4;"
        );

        Label nombre = new Label(texto);

        return new HBox(7, cuadro, nombre);
    }

    private String estiloDisponible() {
        return
            "-fx-background-color: #22a06b;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 7;" +
            "-fx-cursor: hand;";
    }

    private String estiloOcupado() {
        return
            "-fx-background-color: #d94343;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 7;";
    }

    private String estiloSeleccionado() {
        return
            "-fx-background-color: #2878e5;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 7;" +
            "-fx-border-color: #164b9b;" +
            "-fx-border-radius: 7;" +
            "-fx-border-width: 2;";
    }

    private void actualizarResumen() {

        for (javafx.scene.Node nodo : getChildren()) {

            if (nodo instanceof Label label
                    && "resumenAsientos".equals(label.getId())) {

                label.setText(
                    "Asientos seleccionados: "
                    + asientosSeleccionados.size()
                    + " | "
                    + String.join(", ", asientosSeleccionados)
                );
            }
        }
    }

    public Set<String> getAsientosSeleccionados() {
        return new LinkedHashSet<>(asientosSeleccionados);
    }

    public void establecerAsientosOcupados(
            Collection<String> ocupados) {

        asientosOcupados.clear();
        asientosOcupados.addAll(ocupados);

        asientosSeleccionados.clear();

        crearMapa();
        actualizarResumen();
    }

    public void limpiarSeleccion() {

        asientosSeleccionados.clear();
        crearMapa();
        actualizarResumen();
    }
}
