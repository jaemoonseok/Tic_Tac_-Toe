import java.util.*;
import java.net.*;
import java.io.*;

public class TicTacToeServer {
	private Player[] board = new Player[9];

	Player currentPlayer;

	public boolean hasWinner() {
		return (board[0] != null && board[0] == board[1] && board[0] == board[2])
				|| (board[3] != null && board[3] == board[4] && board[3] == board[5])
				|| (board[6] != null && board[6] == board[7] && board[6] == board[8])
				|| (board[0] != null && board[0] == board[3] && board[0] == board[6])
				|| (board[1] != null && board[1] == board[4] && board[1] == board[7])
				|| (board[0] != null && board[0] == board[4] && board[0] == board[8])
				|| (board[2] != null && board[2] == board[5] && board[2] == board[8])
				|| (board[2] != null && board[2] == board[4] && board[2] == board[6]);
	}

	public boolean tie() {
		return Arrays.stream(board).allMatch(p -> p != null);
	}

	public synchronized void move(int location, Player player) {
		if (player != currentPlayer) {
			throw new IllegalStateException("Not your round now.");
		} else if (player.opponent == null) {
			throw new IllegalStateException("You cannot play by yourself.");
		} else if (board[location] != null) {
			throw new IllegalStateException("Cell occupied");
		}
		board[location] = currentPlayer;
		currentPlayer = currentPlayer.opponent;
	}

	class Player implements Runnable {
		char mark;
		Player opponent;
		Socket socket;
		Scanner i;
		PrintWriter o;

		public Player(Socket socket, char mark) {
			this.socket = socket;
			this.mark = mark;
		}

		public void run() {
			try {
				setup();
				processCommands();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (opponent != null && opponent.o != null) {
					opponent.o.println("OTHER_PLAYER_LEFT");
				}
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}

		private void setup() throws IOException {
			i = new Scanner(socket.getInputStream());
			o = new PrintWriter(socket.getOutputStream(), true);
			o.println("WELCOME " + mark);
			if (mark == 'x') {
				currentPlayer = this;
				o.println("MESSAGE Waiting for opponent to connect");
			} else {
				opponent = currentPlayer;
				opponent.opponent = this;
				opponent.o.println("Your move");
			}
		}

		private void processCommands() {
			while (i.hasNextLine()) {
				System.out.println("This is the next Line :" + i.nextLine());
				var command = i.nextLine();
				if (command.startsWith("QUIT")) {
					return;
				} else if (command.startsWith("MOVE")) {
					processMoveCommand(Integer.parseInt(command.substring(5)));
				}
			}
		}

		private void processMoveCommand(int location) {
			try {
				move(location, this);
				o.println("VALID_MOVE");
				opponent.o.println("OPPONENT_MOVED " + location);
				if (hasWinner()) {
					o.println("VICTORY");
					opponent.o.println("DEFEAT");
				} else if (tie()) {
					o.println("TIE");
					opponent.o.println("TIE");

				}
			} catch (IllegalStateException e) {
				o.println("MESSAGE" + e.getMessage());
			}
		}
	}
}