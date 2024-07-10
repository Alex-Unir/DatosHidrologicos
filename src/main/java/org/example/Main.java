package org.example;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name());

        System.out.println("Seleccione una opción:");
        System.out.println("0. Descargar archivo ZIP con datos de embalses");
        System.out.println("1. Nueva inserción");
        System.out.println("2. Actualizar datos");

        int opcion = scanner.nextInt();
        scanner.nextLine();

        switch (opcion) {
            case 0:
                System.out.println("Descargando fichero ZIP...");
                DescargaDatos.carpetas(args);
                break;
            case 1:
                System.out.println("Indica la ruta completa del fichero BD-Embalses.mdb" +
                        " por si es descargado automático está en \\Datoshigrologicos\\FicherosMDB");

                String ruta1 = scanner.nextLine();
                Accesomysql.nuevaInsercion(ruta1);
                break;
            case 2:
                System.out.println("Indica la ruta completa del fichero BD-Embalses.mdb" +
                        " por si es descargado automático está en \\Datoshigrologicos\\FicherosMDB");
                String ruta2 = scanner.nextLine();
                ActualizarDatos.actualizarDatos(ruta2);
                break;
            default:
                System.out.println("Opción no válida.");
        }

        scanner.close();
    }
}
