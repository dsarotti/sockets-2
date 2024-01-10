import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class Cliente {

    public static void main(String[] args) {
        final String SERVIDOR_IP = "127.0.0.1"; // Cambia esto con la IP del servidor
        final int SERVIDOR_PUERTO = 12345;

        try {
            Socket socket = new Socket(SERVIDOR_IP, SERVIDOR_PUERTO);

            // Mostrar información sobre la conexión
            System.out.println("Conectado al servidor");
            System.out.println("Puerto local: " + socket.getLocalPort());
            System.out.println("Puerto remoto: " + socket.getPort());
            System.out.println("Dirección IP remota: " + socket.getInetAddress().getHostAddress());

            // Configurar el flujo de salida para enviar mensajes al servidor
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);

            // Configurar el lector de teclado para recibir las entradas del usuario
            BufferedReader lectorTeclado = new BufferedReader(new InputStreamReader(System.in));

            // Crear un hilo para la lectura de mensajes del servidor
            Thread hiloLectura = new Thread(() -> {
                try {
                    BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String mensaje;
                    while ((mensaje = entrada.readLine()) != null) {
                        System.out.println("Servidor: " + mensaje);
                    }
                } catch (SocketException e) {
                    // Manejar la excepción cuando se cierra el socket
                    System.out.println("Desconectado del servidor.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // Iniciar el hilo de lectura
            hiloLectura.start();

            // Bucle para leer las entradas del usuario y enviarlas al servidor
            String userInput;
            while ((userInput = lectorTeclado.readLine()) != null) {
                salida.println(userInput);

                // Verificar si el usuario quiere cerrar la conexión
                if ("cerrar".equalsIgnoreCase(userInput.trim())) {
                    System.out.println("Cerrando la conexión del cliente.");
                    break;
                }
            }

            // Cerrar la conexión al finalizar
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
