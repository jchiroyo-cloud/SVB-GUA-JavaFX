
package com.svb.gua;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MainApp extends Application {

    private Stage stage;
    private int userId;
    private String username, fullName, role;
    private BorderPane shell;

    private final String css =
            getClass().getResource("/com/svb/gua/styles.css").toExternalForm();

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d/M/uuuu");

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("SVB | Gestión de boletos para conciertos");
        showLogin();
    }

    // ==========================================
    // INICIO DE SESION
    // ==========================================

    private void showLogin() {

        Label brand = new Label("SVB");
        brand.getStyleClass().add("page-title");

        Label title = new Label("Gestión y Venta de Boletos");
        title.setStyle("-fx-font-size: 21px; -fx-font-weight: bold; -fx-text-fill: #172b4d;");

        Label sub = new Label("Sistema local para conciertos");
        sub.getStyleClass().add("page-subtitle");

        TextField user = new TextField();
        user.setPromptText("Nombre de usuario");

        PasswordField pass = new PasswordField();
        pass.setPromptText("Contraseña");

        Label message = new Label();
        message.setStyle("-fx-text-fill: #b42318;");

        Button login = new Button("Iniciar sesión");
        login.getStyleClass().add("primary-button");
        login.setMaxWidth(Double.MAX_VALUE);

        login.setOnAction(e ->
                authenticate(user.getText().trim(), pass.getText(), message)
        );

        pass.setOnAction(e -> login.fire());

        VBox form = new VBox(
                12,
                brand,
                title,
                sub,
                new Separator(),
                new Label("Usuario"),
                user,
                new Label("Contraseña"),
                pass,
                login,
                message
        );

        form.setPadding(new Insets(30));
        form.getStyleClass().add("card");
        form.setMaxWidth(410);

        StackPane root = new StackPane(form);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #e9f0fa, #f7f9fc);");

        Scene scene = new Scene(root, 980, 650);
        scene.getStylesheets().add(css);

        stage.setScene(scene);
        stage.setMinWidth(760);
        stage.setMinHeight(540);
        stage.centerOnScreen();
        stage.show();
    }

    private void authenticate(String u, String p, Label message) {

        if (u.isBlank() || p.isBlank()) {
            message.setText("Escribe el usuario y la contraseña.");
            return;
        }

        // CORREGIDO: la columna correcta es contrasena_hash.
        String sql = "SELECT id_usuario,nombre_usuario,nombre_completo,rol "
                + "FROM usuarios WHERE nombre_usuario=? AND contrasena_hash=?";

        try (Connection c = Db.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, u);
            ps.setString(2, Db.hash(p));

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    userId = rs.getInt(1);
                    username = rs.getString(2);
                    fullName = rs.getString(3);
                    role = rs.getString(4);
                    showDashboard();
                } else {
                    message.setText("Usuario o contraseña incorrectos.");
                }
            }

        } catch (Exception ex) {
            message.setText("No se pudo conectar con PostgreSQL: " + ex.getMessage());
        }
    }

    // ==========================================
    // PANEL PRINCIPAL
    // ==========================================

    private void showDashboard() {

        shell = new BorderPane();

        VBox sidebar = new VBox(8);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);

        Label brand = new Label("SVB");
        brand.getStyleClass().add("brand");

        Label subtitle = new Label("BOLETOS PARA CONCIERTOS");
        subtitle.getStyleClass().add("brand-sub");

        sidebar.getChildren().addAll(brand, subtitle, new Separator());

        Label who = new Label(fullName + "\n" + role);
        who.setStyle("-fx-text-fill: #dce7f5; -fx-padding: 12 5 12 5;");

        addNav(sidebar, "Resumen", () -> showPage(dashboardPage()));

        if ("administrador".equalsIgnoreCase(role)) {

            addNav(sidebar, "Artistas", () ->
                    showPage(new CrudPage("Artistas", "artistas",
                            new String[]{"id_artista", "nombre_artistico",
                                "genero_musical", "pais_origen"}, true)));

            addNav(sidebar, "Conciertos", () ->
                    showPage(new CrudPage("Conciertos", "conciertos",
                            new String[]{"id_concierto", "id_artista",
                                "titulo_evento", "fecha_concierto",
                                "recinto", "estado"}, true)));

            addNav(sidebar, "Localidades", () ->
                    showPage(new CrudPage("Localidades", "localidades",
                            new String[]{"id_localidad", "nombre"}, true)));

            addNav(sidebar, "Inventario", () ->
                    showPage(new CrudPage("Inventario de boletos",
                            "inventario_boletos",
                            new String[]{"id_inventario", "id_concierto",
                                "id_localidad", "precio",
                                "cantidad_total", "cantidad_disponible"}, true)));

            addNav(sidebar, "Usuarios", () ->
                    showPage(new CrudPage("Usuarios", "usuarios",
                            new String[]{"id_usuario", "nombre_usuario",
                                "nombre_completo", "rol",
                                "fecha_creacion"}, true)));
        }

        addNav(sidebar, "Vender boletos", () ->
                showPage(new SalesPage()));

        addNav(sidebar, "Reportes", () ->
                showPage(new ReportsPage()));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logout = new Button("Cerrar sesión");
        logout.getStyleClass().add("nav-button");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setOnAction(e -> showLogin());

        sidebar.getChildren().addAll(spacer, who, logout);

        shell.setLeft(sidebar);
        shell.setCenter(dashboardPage());

        Scene scene = new Scene(shell, 1250, 780);
        scene.getStylesheets().add(css);

        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.centerOnScreen();
    }

    private void addNav(VBox sidebar, String text, Runnable action) {

        Button b = new Button(text);
        b.getStyleClass().add("nav-button");
        b.setMaxWidth(Double.MAX_VALUE);
        b.setOnAction(e -> action.run());

        sidebar.getChildren().add(b);
    }

    private void showPage(javafx.scene.Node page) {
        shell.setCenter(page);
    }

    private VBox pageHeader(String title, String subtitle) {

        Label t = new Label(title);
        t.getStyleClass().add("page-title");

        Label s = new Label(subtitle);
        s.getStyleClass().add("page-subtitle");

        VBox box = new VBox(5, t, s);
        box.setPadding(new Insets(0, 0, 18, 0));

        return box;
    }

    // ==========================================
    // PANEL DE RESUMEN
    // ==========================================

    private javafx.scene.Node dashboardPage() {

        VBox root = new VBox(18);
        root.setPadding(new Insets(28));

        root.getChildren().add(pageHeader(
                "Panel principal",
                "Resumen general del sistema de venta de boletos."
        ));

        HBox stats = new HBox(14);

        stats.getChildren().addAll(
                statCard("Conciertos", count("SELECT COUNT(*) FROM conciertos")),
                statCard("Boletos disponibles",
                        count("SELECT COALESCE(SUM(cantidad_disponible),0) FROM inventario_boletos")),
                statCard("Ventas registradas",
                        count("SELECT COUNT(*) FROM ventas")),
                statCard("Ingresos acumulados",
                        "Q " + money(scalar("SELECT COALESCE(SUM(total_venta),0) FROM ventas")))
        );

        for (javafx.scene.Node n : stats.getChildren()) {
            HBox.setHgrow(n, Priority.ALWAYS);
        }

        VBox recent = new VBox(10);
        recent.getStyleClass().add("card");

        Label heading = new Label("Ventas recientes");
        heading.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #203653;");

        TableView<ObservableList<String>> table = queryTable(
                "SELECT v.id_venta, v.fecha_venta, u.nombre_usuario, v.total_venta "
                + "FROM ventas v JOIN usuarios u ON u.id_usuario=v.id_vendedor "
                + "ORDER BY v.id_venta DESC LIMIT 8",
                new String[]{"Venta", "Fecha", "Vendedor", "Total (Q)"}
        );

        recent.getChildren().addAll(heading, table);
        VBox.setVgrow(recent, Priority.ALWAYS);

        root.getChildren().addAll(stats, recent);

        return root;
    }

    private VBox statCard(String label, String value) {

        Label l = new Label(label);
        l.getStyleClass().add("stat-label");

        Label v = new Label(value);
        v.getStyleClass().add("stat-value");

        VBox card = new VBox(12, l, v);
        card.getStyleClass().add("card");
        card.setMinWidth(150);

        return card;
    }

    private String count(String sql) {
        return String.valueOf((long) scalar(sql).doubleValue());
    }

    private BigDecimal scalar(String sql) {

        try (Connection c = Db.connect();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {

            if (r.next()) {
                return r.getBigDecimal(1) == null
                        ? BigDecimal.ZERO : r.getBigDecimal(1);
            }

        } catch (Exception ignored) {
        }

        return BigDecimal.ZERO;
    }

    private String money(BigDecimal b) {
        return b.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    // ==========================================
    // ADMINISTRACION CRUD
    // ==========================================

    private class CrudPage extends VBox {

        final String title;
        final String tableName;
        final String[] cols;
        final boolean admin;

        TableView<ObservableList<String>> table;

        CrudPage(String title, String tableName, String[] cols, boolean admin) {

            this.title = title;
            this.tableName = tableName;
            this.cols = cols;
            this.admin = admin;

            setSpacing(14);
            setPadding(new Insets(28));

            getChildren().add(pageHeader(
                    title,
                    "Administra los registros de " + title.toLowerCase() + "."
            ));

            table = new TableView<>();
            VBox.setVgrow(table, Priority.ALWAYS);
            getChildren().add(table);

            HBox actions = new HBox(9);

            Button add = button("Agregar", true);
            Button edit = button("Editar", false);
            Button del = button("Eliminar", false);
            Button refresh = button("Actualizar", false);

            actions.getChildren().addAll(add, edit, del, refresh);
            getChildren().add(actions);

            add.setOnAction(e -> addRecord());
            edit.setOnAction(e -> editRecord());
            del.setOnAction(e -> deleteRecord());
            refresh.setOnAction(e -> load());

            load();
        }

        void load() {

            String sql = "SELECT " + String.join(",", cols)
                    + " FROM " + tableName
                    + " ORDER BY " + cols[0];

            fillTable(table, sql, cols);
        }

        void addRecord() {

            List<String> fields = new ArrayList<>(Arrays.asList(cols));
            fields.remove(0);

            if (tableName.equals("usuarios")) {
                fields.remove("fecha_creacion");
            }

            Map<String, String> values =
                    editDialog("Nuevo registro", fields, Map.of());

            if (values == null) {
                return;
            }

            try (Connection c = Db.connect()) {

                if (tableName.equals("usuarios")) {

                    TextInputDialog d = new TextInputDialog();
                    d.setTitle("Contraseña de usuario");
                    d.setHeaderText("Define una contraseña para el nuevo usuario");
                    d.setContentText("Contraseña:");

                    Optional<String> pwd = d.showAndWait();

                    if (pwd.isEmpty() || pwd.get().isBlank()) {
                        return;
                    }

                    try (PreparedStatement p = c.prepareStatement(
                            "INSERT INTO usuarios(nombre_usuario,nombre_completo,rol,contrasena_hash) VALUES (?,?,?,?)")) {

                        p.setString(1, values.get("nombre_usuario"));
                        p.setString(2, values.get("nombre_completo"));
                        p.setString(3, values.get("rol"));
                        p.setString(4, Db.hash(pwd.get()));

                        p.executeUpdate();
                    }

                } else {

                    String sql = "INSERT INTO " + tableName
                            + " (" + String.join(",", fields) + ") VALUES ("
                            + String.join(",", Collections.nCopies(fields.size(), "?")) + ")";

                    try (PreparedStatement p = c.prepareStatement(sql)) {
                        bind(p, fields, values);
                        p.executeUpdate();
                    }
                }

                load();
                alert("Registro agregado correctamente.");

            } catch (Exception ex) {
                alertError("No se pudo agregar el registro", ex);
            }
        }

        void editRecord() {

            ObservableList<String> row =
                    table.getSelectionModel().getSelectedItem();

            if (row == null) {
                alert("Selecciona un registro primero.");
                return;
            }

            List<String> fields = new ArrayList<>(Arrays.asList(cols));
            fields.remove(0);

            if (tableName.equals("usuarios")) {
                fields.remove("fecha_creacion");
            }

            Map<String, String> defaults = new LinkedHashMap<>();

            for (String f : fields) {

                String value = row.get(Arrays.asList(cols).indexOf(f));

                if (f.equals("fecha_concierto")) {
                    value = mostrarFecha(value);
                }

                defaults.put(f, value);
            }

            Map<String, String> values =
                    editDialog("Editar registro", fields, defaults);

            if (values == null) {
                return;
            }

            if (tableName.equals("conciertos")) {

                values.put("__hora_original",
                        obtenerHoraOriginal(Integer.parseInt(row.get(0))));
            }

            try (Connection c = Db.connect()) {

                String sql = "UPDATE " + tableName
                        + " SET " + String.join("=?,", fields)
                        + "=? WHERE " + cols[0] + "=?";

                try (PreparedStatement p = c.prepareStatement(sql)) {

                    bind(p, fields, values);

                    p.setInt(fields.size() + 1,
                            Integer.parseInt(row.get(0)));

                    p.executeUpdate();
                }

                load();
                alert("Registro actualizado.");

            } catch (Exception ex) {
                alertError("No se pudo actualizar", ex);
            }
        }

        void deleteRecord() {

            ObservableList<String> row =
                    table.getSelectionModel().getSelectedItem();

            if (row == null) {
                alert("Selecciona un registro primero.");
                return;
            }

            Alert confirm = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "¿Eliminar el registro seleccionado?",
                    ButtonType.CANCEL,
                    ButtonType.OK
            );

            confirm.setHeaderText("Confirmar eliminación");

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }

            try (Connection c = Db.connect();
                 PreparedStatement p = c.prepareStatement(
                         "DELETE FROM " + tableName + " WHERE " + cols[0] + "=?")) {

                p.setInt(1, Integer.parseInt(row.get(0)));
                p.executeUpdate();

                load();

            } catch (Exception ex) {
                alertError("No se pudo eliminar. Puede existir información relacionada", ex);
            }
        }
    }

    // ==========================================
    // FORMULARIOS
    // ==========================================

    private Map<String, String> editDialog(
            String title,
            List<String> fields,
            Map<String, String> defaults) {

        Dialog<Map<String, String>> dialog = new Dialog<>();

        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.OK,
                ButtonType.CANCEL
        );

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        Map<String, TextField> inputs = new LinkedHashMap<>();

        int row = 0;

        for (String f : fields) {

            TextField tf = new TextField(defaults.getOrDefault(f, ""));
            tf.setPrefWidth(300);
            tf.setPromptText(promptFor(f));

            grid.add(new Label(f), 0, row);
            grid.add(tf, 1, row++);

            inputs.put(f, tf);
        }

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(bt -> {

            if (bt != ButtonType.OK) {
                return null;
            }

            Map<String, String> result = new LinkedHashMap<>();

            inputs.forEach((k, v) ->
                    result.put(k, v.getText().trim())
            );

            return result;
        });

        Optional<Map<String, String>> result = dialog.showAndWait();

        return result.orElse(null);
    }

    private String promptFor(String field) {

        if (field.equals("fecha_concierto")) {
            return "D/M/AAAA (ejemplo: 4/11/2026)";
        }

        if (field.startsWith("id_")) {
            return "ID relacionado";
        }

        if (field.equals("estado")) {
            return "programado / activo / finalizado / cancelado";
        }

        if (field.equals("rol")) {
            return "administrador / vendedor";
        }

        return field;
    }

    // ==========================================
    // CONVERSION Y GUARDADO DE FECHAS
    // ==========================================

    private String mostrarFecha(String fecha) {

        if (fecha == null || fecha.isBlank()) {
            return "";
        }

        try {

            Timestamp timestamp = Timestamp.valueOf(fecha);

            return timestamp.toLocalDateTime().format(DATE_FORMAT);

        } catch (Exception e) {
            return fecha;
        }
    }

    private String obtenerHoraOriginal(int idConcierto) {

        String sql = "SELECT fecha_concierto FROM conciertos WHERE id_concierto=?";

        try (Connection c = Db.connect();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, idConcierto);

            try (ResultSet rs = p.executeQuery()) {

                if (rs.next()) {
                    Timestamp fecha = rs.getTimestamp(1);

                    return fecha == null ? "" : fecha.toString();
                }
            }

        } catch (Exception ex) {
            throw new RuntimeException("No se pudo recuperar la hora original.", ex);
        }

        return "";
    }

    private void bind(
            PreparedStatement p,
            List<String> fields,
            Map<String, String> vals
    ) throws Exception {

        for (int i = 0; i < fields.size(); i++) {

            String f = fields.get(i);
            String v = vals.getOrDefault(f, "");

            if (v.isBlank()) {

                p.setNull(i + 1, Types.NULL);

            } else if (f.startsWith("id_") || f.startsWith("cantidad_")) {

                p.setInt(i + 1, Integer.parseInt(v));

            } else if (f.equals("precio")) {

                p.setBigDecimal(i + 1, new BigDecimal(v));

            } else if (f.equals("fecha_concierto")) {

                LocalDate fecha = LocalDate.parse(v, DATE_FORMAT);

                String horaOriginal = vals.get("__hora_original");

                LocalDateTime fechaHora;

                if (horaOriginal != null && !horaOriginal.isBlank()) {

                    Timestamp anterior = Timestamp.valueOf(horaOriginal);

                    fechaHora = fecha.atTime(
                            anterior.toLocalDateTime().toLocalTime()
                    );

                } else {

                    fechaHora = fecha.atStartOfDay();
                }

                p.setTimestamp(i + 1, Timestamp.valueOf(fechaHora));

            } else {

                p.setString(i + 1, v);
            }
        }
    }

    // ==========================================
    // VENTAS
    // ==========================================

    private class SalesPage extends VBox {

        ComboBox<ConcertItem> concert = new ComboBox<>();
        ComboBox<InventoryItem> locality = new ComboBox<>();

        Spinner<Integer> qty = new Spinner<>(1, 10000, 1);

        TableView<SaleLine> cart = new TableView<>();

        ObservableList<SaleLine> lines =
                FXCollections.observableArrayList();

        Label total = new Label("Total: Q 0.00");
        Label inventoryInfo = new Label("Selecciona una localidad.");

        SalesPage() {

            setSpacing(14);
            setPadding(new Insets(28));

            getChildren().add(pageHeader(
                    "Venta de boletos",
                    "Selecciona un concierto, una localidad y la cantidad de boletos."
            ));

            concert.setPrefWidth(350);
            concert.setPromptText("Seleccionar concierto");

            locality.setPrefWidth(280);
            locality.setPromptText("Seleccionar localidad");

            Button add = button("Agregar al carrito", true);

            GridPane selection = new GridPane();
            selection.setHgap(12);
            selection.setVgap(10);
            selection.setPadding(new Insets(15));
            selection.getStyleClass().add("card");

            selection.add(new Label("Concierto"), 0, 0);
            selection.add(concert, 1, 0);

            selection.add(new Label("Localidad"), 2, 0);
            selection.add(locality, 3, 0);

            selection.add(new Label("Cantidad"), 4, 0);
            selection.add(qty, 5, 0);

            selection.add(add, 6, 0);

            selection.add(inventoryInfo, 1, 1, 5, 1);

            ColumnConstraints c1 = new ColumnConstraints();
            c1.setHgrow(Priority.NEVER);

            ColumnConstraints c2 = new ColumnConstraints();
            c2.setHgrow(Priority.ALWAYS);

            selection.getColumnConstraints().addAll(
                    c1, c2,
                    c1, c2,
                    c1, c1,
                    c1
            );

            TableColumn<SaleLine, String> event =
                    saleCol("Evento / localidad", x -> x.label);

            TableColumn<SaleLine, String> price =
                    saleCol("Precio", x -> money(x.price));

            TableColumn<SaleLine, String> quantity =
                    saleCol("Cantidad", x -> String.valueOf(x.qty));

            TableColumn<SaleLine, String> subtotal =
                    saleCol("Subtotal",
                            x -> money(x.price.multiply(BigDecimal.valueOf(x.qty))));

            cart.getColumns().addAll(event, price, quantity, subtotal);
            cart.setItems(lines);

            VBox.setVgrow(cart, Priority.ALWAYS);

            Button remove = button("Quitar seleccionado", false);
            Button finish = button("Confirmar venta", true);
            Button refresh = button("Actualizar inventario", false);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            total.setStyle("-fx-font-size: 18px;-fx-font-weight:bold;-fx-text-fill:#172b4d;");

            HBox bottom = new HBox(12, remove, refresh, spacer, total, finish);
            bottom.setAlignment(Pos.CENTER_RIGHT);

            getChildren().addAll(selection, cart, bottom);

            concert.setOnAction(e -> loadLocalities());
            locality.setOnAction(e -> showInventoryInfo());

            add.setOnAction(e -> addLine());

            remove.setOnAction(e -> {
                SaleLine l = cart.getSelectionModel().getSelectedItem();

                if (l != null) {
                    lines.remove(l);
                    updateTotal();
                }
            });

            refresh.setOnAction(e -> loadInventory());

            finish.setOnAction(e -> sell());

            loadInventory();
        }

        void loadInventory() {

            concert.getItems().clear();
            locality.getItems().clear();
            inventoryInfo.setText("Selecciona una localidad.");

            String sql =
                    "SELECT c.id_concierto, c.titulo_evento "
                    + "FROM conciertos c "
                    + "WHERE c.estado IN ('programado','activo') "
                    + "AND EXISTS ("
                    + " SELECT 1 FROM inventario_boletos i "
                    + " WHERE i.id_concierto=c.id_concierto "
                    + " AND i.cantidad_disponible>0"
                    + ") "
                    + "ORDER BY c.fecha_concierto";

            try (Connection c = Db.connect();
                 Statement s = c.createStatement();
                 ResultSet r = s.executeQuery(sql)) {

                while (r.next()) {

                    concert.getItems().add(
                            new ConcertItem(
                                    r.getInt("id_concierto"),
                                    r.getString("titulo_evento")
                            )
                    );
                }

                if (!concert.getItems().isEmpty()) {
                    concert.getSelectionModel().selectFirst();
                    loadLocalities();
                } else {
                    inventoryInfo.setText(
                            "No hay conciertos con boletos disponibles."
                    );
                }

            } catch (Exception ex) {
                alertError("No se pudieron cargar los conciertos", ex);
            }
        }

        void loadLocalities() {

            locality.getItems().clear();
            inventoryInfo.setText("Selecciona una localidad.");

            ConcertItem selectedConcert = concert.getValue();

            if (selectedConcert == null) {
                return;
            }

            String sql =
                    "SELECT i.id_inventario, l.nombre, "
                    + "i.precio, i.cantidad_disponible "
                    + "FROM inventario_boletos i "
                    + "JOIN localidades l ON l.id_localidad=i.id_localidad "
                    + "WHERE i.id_concierto=? "
                    + "ORDER BY l.id_localidad";

            try (Connection c = Db.connect();
                 PreparedStatement p = c.prepareStatement(sql)) {

                p.setInt(1, selectedConcert.id);

                try (ResultSet r = p.executeQuery()) {

                    while (r.next()) {

                        locality.getItems().add(
                                new InventoryItem(
                                        r.getInt("id_inventario"),
                                        r.getString("nombre"),
                                        r.getBigDecimal("precio"),
                                        r.getInt("cantidad_disponible")
                                )
                        );
                    }
                }

                if (!locality.getItems().isEmpty()) {

                    locality.getSelectionModel().selectFirst();
                    showInventoryInfo();

                } else {

                    inventoryInfo.setText(
                            "Este concierto todavía no tiene localidades registradas."
                    );
                }

            } catch (Exception ex) {
                alertError("No se pudieron cargar las localidades", ex);
            }
        }

        void showInventoryInfo() {

            InventoryItem it = locality.getValue();

            if (it == null) {
                inventoryInfo.setText("Selecciona una localidad.");
                return;
            }

            inventoryInfo.setText(
                    "Precio: Q " + money(it.price)
                    + "     |     Disponibles: " + it.available
            );
        }

        void addLine() {

            ConcertItem selectedConcert = concert.getValue();
            InventoryItem it = locality.getValue();

            if (selectedConcert == null) {
                alert("Primero selecciona un concierto.");
                return;
            }

            if (it == null) {
                alert("Selecciona una localidad.");
                return;
            }

            int n = qty.getValue();

            if (it.available <= 0) {
                alert("Esta localidad ya no tiene boletos disponibles.");
                return;
            }

            if (n > it.available) {
                alert("Solo hay " + it.available + " boletos disponibles.");
                return;
            }

            String label =
                    selectedConcert.name + " / " + it.label;

            for (SaleLine line : lines) {

                if (line.id == it.id) {

                    if (line.qty + n > it.available) {
                        alert("La cantidad supera el inventario disponible.");
                        return;
                    }

                    line.qty += n;
                    cart.refresh();
                    updateTotal();

                    return;
                }
            }

            lines.add(
                    new SaleLine(
                            it.id,
                            label,
                            it.price,
                            n,
                            it.available
                    )
            );

            updateTotal();
        }

        BigDecimal totalValue() {

            return lines.stream()
                    .map(x -> x.price.multiply(BigDecimal.valueOf(x.qty)))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        void updateTotal() {
            total.setText("Total: Q " + money(totalValue()));
        }

        void sell() {

            if (lines.isEmpty()) {
                alert("Agrega al menos un boleto.");
                return;
            }

            Alert a = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "¿Confirmar venta por Q " + money(totalValue()) + "?",
                    ButtonType.CANCEL,
                    ButtonType.OK
            );

            a.setHeaderText("Confirmar venta");

            if (a.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }

            try (Connection c = Db.connect()) {

                c.setAutoCommit(false);

                try {

                    int saleId;

                    try (PreparedStatement p = c.prepareStatement(
                            "INSERT INTO ventas(id_vendedor,total_venta) VALUES (?,?) RETURNING id_venta")) {

                        p.setInt(1, userId);
                        p.setBigDecimal(2, totalValue());

                        try (ResultSet r = p.executeQuery()) {
                            r.next();
                            saleId = r.getInt(1);
                        }
                    }

                    for (SaleLine line : lines) {

                        try (PreparedStatement p = c.prepareStatement(
                                "UPDATE inventario_boletos "
                                + "SET cantidad_disponible=cantidad_disponible-? "
                                + "WHERE id_inventario=? AND cantidad_disponible>=?")) {

                            p.setInt(1, line.qty);
                            p.setInt(2, line.id);
                            p.setInt(3, line.qty);

                            if (p.executeUpdate() != 1) {
                                throw new SQLException(
                                        "Inventario insuficiente para " + line.label
                                );
                            }
                        }

                        try (PreparedStatement p = c.prepareStatement(
                                "INSERT INTO detalle_ventas"
                                + "(id_venta,id_inventario,cantidad,precio_unitario,subtotal) "
                                + "VALUES (?,?,?,?,?)")) {

                            p.setInt(1, saleId);
                            p.setInt(2, line.id);
                            p.setInt(3, line.qty);
                            p.setBigDecimal(4, line.price);
                            p.setBigDecimal(5,
                                    line.price.multiply(BigDecimal.valueOf(line.qty)));

                            p.executeUpdate();
                        }
                    }

                    c.commit();

                    lines.clear();
                    cart.refresh();
                    updateTotal();

                    loadInventory();

                    alert(
                            "Venta registrada correctamente. Número de venta: "
                            + saleId
                    );

                } catch (Exception ex) {

                    c.rollback();
                    throw ex;

                } finally {

                    c.setAutoCommit(true);
                }

            } catch (Exception ex) {
                alertError("No se pudo registrar la venta", ex);
            }
        }
    }

    // ==========================================
    // MODELOS DE VENTA
    // ==========================================

    private static class ConcertItem {

        int id;
        String name;

        ConcertItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class InventoryItem {

        int id;
        int available;
        String label;
        BigDecimal price;

        InventoryItem(int id, String label, BigDecimal price, int available) {
            this.id = id;
            this.label = label;
            this.price = price;
            this.available = available;
        }

        @Override
        public String toString() {
            return label
                    + " | Q " + price
                    + " | disponibles: " + available;
        }
    }

    private static class SaleLine {

        int id;
        int qty;
        int available;
        String label;
        BigDecimal price;

        SaleLine(int id, String label, BigDecimal price, int qty, int available) {
            this.id = id;
            this.label = label;
            this.price = price;
            this.qty = qty;
            this.available = available;
        }
    }

    // ==========================================
    // REPORTES
    // ==========================================

    private class ReportsPage extends VBox {

        ComboBox<String> report = new ComboBox<>(
                FXCollections.observableArrayList(
                        "Ventas recientes",
                        "Ventas por concierto",
                        "Ventas por artista",
                        "Ventas por vendedor",
                        "Inventario disponible"
                )
        );

        TableView<ObservableList<String>> table = new TableView<>();

        ReportsPage() {

            setSpacing(14);
            setPadding(new Insets(28));

            getChildren().add(pageHeader(
                    "Reportes",
                    "Consulta ventas, ingresos e inventario disponible."
            ));

            report.getSelectionModel().selectFirst();

            Button run = button("Generar reporte", true);

            HBox bar = new HBox(
                    10,
                    new Label("Tipo de reporte"),
                    report,
                    run
            );

            bar.setAlignment(Pos.CENTER_LEFT);

            getChildren().addAll(bar, table);
            VBox.setVgrow(table, Priority.ALWAYS);

            run.setOnAction(e -> runReport());

            runReport();
        }

        void runReport() {

            String selected = report.getValue();
            String sql;
            String[] headers;

            if ("Ventas por concierto".equals(selected)) {

                sql = "SELECT c.titulo_evento, SUM(d.cantidad), SUM(d.subtotal) "
                        + "FROM detalle_ventas d "
                        + "JOIN inventario_boletos i USING(id_inventario) "
                        + "JOIN conciertos c USING(id_concierto) "
                        + "GROUP BY c.titulo_evento ORDER BY SUM(d.subtotal) DESC";

                headers = new String[]{
                    "Concierto", "Boletos vendidos", "Recaudación (Q)"
                };

            } else if ("Ventas por artista".equals(selected)) {

                sql = "SELECT a.nombre_artistico,SUM(d.cantidad),SUM(d.subtotal) "
                        + "FROM detalle_ventas d "
                        + "JOIN inventario_boletos i USING(id_inventario) "
                        + "JOIN conciertos c USING(id_concierto) "
                        + "JOIN artistas a USING(id_artista) "
                        + "GROUP BY a.nombre_artistico ORDER BY SUM(d.subtotal) DESC";

                headers = new String[]{
                    "Artista", "Boletos vendidos", "Recaudación (Q)"
                };

            } else if ("Ventas por vendedor".equals(selected)) {

                sql = "SELECT u.nombre_completo,COUNT(v.id_venta),SUM(v.total_venta) "
                        + "FROM ventas v "
                        + "JOIN usuarios u ON u.id_usuario=v.id_vendedor "
                        + "GROUP BY u.nombre_completo ORDER BY SUM(v.total_venta) DESC";

                headers = new String[]{
                    "Vendedor", "Ventas", "Total (Q)"
                };

            } else if ("Inventario disponible".equals(selected)) {

                sql = "SELECT c.titulo_evento,l.nombre,i.precio,"
                        + "i.cantidad_total,i.cantidad_disponible "
                        + "FROM inventario_boletos i "
                        + "JOIN conciertos c USING(id_concierto) "
                        + "JOIN localidades l USING(id_localidad) "
                        + "ORDER BY c.fecha_concierto";

                headers = new String[]{
                    "Concierto", "Localidad", "Precio (Q)",
                    "Cantidad total", "Disponibles"
                };

            } else {

                sql = "SELECT v.id_venta,v.fecha_venta,u.nombre_completo,v.total_venta "
                        + "FROM ventas v "
                        + "JOIN usuarios u ON u.id_usuario=v.id_vendedor "
                        + "ORDER BY v.id_venta DESC LIMIT 200";

                headers = new String[]{
                    "Venta", "Fecha", "Vendedor", "Total (Q)"
                };
            }

            fillTable(table, sql, headers);
        }
    }

    // ==========================================
    // COMPONENTES GENERALES
    // ==========================================

    private Button button(String text, boolean primary) {

        Button b = new Button(text);

        b.getStyleClass().add(
                primary ? "primary-button" : "secondary-button"
        );

        return b;
    }

    private TableColumn<SaleLine, String> saleCol(
            String title,
            java.util.function.Function<SaleLine, String> fn) {

        TableColumn<SaleLine, String> c = new TableColumn<>(title);

        c.setCellValueFactory(data ->
                new SimpleStringProperty(fn.apply(data.getValue()))
        );

        c.setPrefWidth(250);

        return c;
    }

    private TableView<ObservableList<String>> queryTable(
            String sql,
            String[] headers) {

        TableView<ObservableList<String>> t = new TableView<>();

        fillTable(t, sql, headers);

        return t;
    }

    private void fillTable(
            TableView<ObservableList<String>> table,
            String sql,
            String[] headers) {

        table.getColumns().clear();
        table.getItems().clear();

        for (int i = 0; i < headers.length; i++) {

            final int index = i;

            TableColumn<ObservableList<String>, String> c =
                    new TableColumn<>(headers[i]);

            c.setCellValueFactory(data ->
                    new SimpleStringProperty(
                            index < data.getValue().size()
                            ? data.getValue().get(index) : ""
                    )
            );

            c.setPrefWidth(Math.max(130, 900.0 / headers.length));

            table.getColumns().add(c);
        }

        try (Connection con = Db.connect();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {

                ObservableList<String> row =
                        FXCollections.observableArrayList();

                for (int i = 1; i <= headers.length; i++) {

                    Object v = rs.getObject(i);

                    if (v != null && headers[i - 1].equals("fecha_concierto")) {

                        row.add(mostrarFecha(v.toString()));

                    } else {

                        row.add(v == null ? "" : v.toString());
                    }
                }

                table.getItems().add(row);
            }

        } catch (Exception ex) {

            if (stage != null) {
                System.err.println("Consulta: " + ex.getMessage());
            }
        }
    }

    // ==========================================
    // MENSAJES
    // ==========================================

    private void alert(String message) {

        Alert a = new Alert(
                Alert.AlertType.INFORMATION,
                message,
                ButtonType.OK
        );

        a.setHeaderText(null);
        a.showAndWait();
    }

    private void alertError(String title, Exception ex) {

        Alert a = new Alert(
                Alert.AlertType.ERROR,
                title + ".\n" + ex.getMessage(),
                ButtonType.OK
        );

        a.setHeaderText("Ocurrió un problema");
        a.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

