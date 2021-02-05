import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerLauncher {

	public static void main(String[] args) throws Exception {
		try (ServerSocket listener = new ServerSocket(5000)) {
			System.out.println("Tic Tac Toe Server is Running... ");
			ExecutorService pool = Executors.newFixedThreadPool(200);
			while (true) {
				TicTacToeServer server = new TicTacToeServer();
				pool.execute(server.new Player(listener.accept(), 'x'));
				pool.execute(server.new Player(listener.accept(), 'o'));
			}
		}
	}
}