import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Servidor {

    public static void main(String[] args) {
        final int PUERTO = 12345;

        try {
            ServerSocket servidorSocket = new ServerSocket(PUERTO);
            new Thread(new GestorSockets(servidorSocket)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clase interna para aceptar conexiones de clientes en bucle creando un hilo
     * por cada conexión aceptada.
     */
    static class GestorSockets implements Runnable {
        private ServerSocket server;

        // Se utiliza AtomicInteger para poder actualizar el valor evitando problemas de
        // concurrencia.
        public static AtomicInteger contadorClientes = new AtomicInteger(0);
        private static int clienteId = 0;

        public GestorSockets(ServerSocket server) {
            this.server = server;
        }

        @Override
        public void run() {
            while (true) {
                // Este programa acepta un máximo de 2 conexiones simultáneas
                if (GestorSockets.contadorClientes.get() < 2) {
                    new Thread(new ManejadorCliente(server, "Cliente " + clienteId++)).start();
                    contadorClientes.incrementAndGet();

                }
            }
        }
    }

    // Clase interna para manejar la comunicación con cada cliente
    static class ManejadorCliente implements Runnable {
        private ServerSocket server;
        private Socket socketCliente;
        private String nombreCliente;

        public ManejadorCliente(ServerSocket server, String nombreCliente) {
            this.server = server;
            this.nombreCliente = nombreCliente;
        }

        @Override
        public void run() {
            try {

                System.out.println("Esperando conexión para el cliente " + nombreCliente);
                socketCliente = server.accept();

                System.out.println("Cliente " + nombreCliente + "conectado!");
                Scanner entrada = new Scanner(socketCliente.getInputStream());
                PrintWriter salida = new PrintWriter(socketCliente.getOutputStream(), true);

                salida.println("¡Bienvenido, " + nombreCliente + "!");

                // Escuchar mensajes del cliente y enviarlos de vuelta
                while (true) {
                    String mensaje = entrada.nextLine();

                    // Comprobar si el mensaje indica que el cliente desea cerrar la conexión
                    if ("cerrar".equalsIgnoreCase(mensaje.trim())) {
                        System.out.println(nombreCliente + " ha cerrado la conexión.");

                        // Al cerrar una conexión decrementa el contador de clientes del gestor de
                        // sockets.
                        GestorSockets.contadorClientes.decrementAndGet();
                        break; // Salir del bucle y finalizar el hilo
                    }
                    System.out.println(nombreCliente + ": " + mensaje);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    // Cerrar el socket cuando el cliente cierra la conexión
                    socketCliente.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}