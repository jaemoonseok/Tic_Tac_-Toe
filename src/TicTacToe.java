import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.*;
import java.io.*;

public class TicTacToe {
	int count = 0;
	JFrame frame = new JFrame("Tic Tac Toe");
	JPanel panel = new JPanel();
	JPanel board = new JPanel();
	JPanel submit = new JPanel();
	JButton[] buttonArr = new JButton[9];
	JButton buttonSubmit = new JButton("Submit");
	JLabel label = new JLabel("Enter your player name...");
	JTextField text = new JTextField(15);
	boolean enabled = false;
	JButton currentButton;
	private Socket socket;
	private Scanner i;
	private PrintWriter o;

	public TicTacToe() throws Exception {

		socket = new Socket("127.0.0.1", 5000);
		i = new Scanner(socket.getInputStream());
		o = new PrintWriter(socket.getOutputStream(), true);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JMenuBar menuBar = new JMenuBar();
		JMenu control = new JMenu("Control");
		JMenu help = new JMenu("Help");
		JMenuItem quit = new JMenuItem("Exit");
		JMenuItem rule = new JMenuItem("Instructions");
		quit.addActionListener(new ExitBtn());
		rule.addActionListener(new InstructionsBtn());
		control.add(quit);
		help.add(rule);
		menuBar.add(control);
		menuBar.add(help);

		board.setLayout(new GridLayout(3, 3));
		panel.setLayout(new BorderLayout());
		panel.add(label, BorderLayout.NORTH);

		for (int i = 0; i < 9; i++) {
			final int j = i;
			buttonArr[i] = new JButton();
			buttonArr[i].setBackground(Color.WHITE);
			buttonArr[i].setEnabled(false);
			buttonArr[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					System.out.println("Clicked");
					if (enabled) {
						o.println("MOVE " + j);
						currentButton = buttonArr[j];
					}
				}

			});
			board.add(buttonArr[i]);
		}

		buttonSubmit.addActionListener(new SubmitBtn());

		panel.add(board, BorderLayout.CENTER);

		submit.add(text);
		submit.add(buttonSubmit);
		panel.add(submit, BorderLayout.SOUTH);

		frame.add(panel);
		frame.setJMenuBar(menuBar);
		frame.setSize(300, 300);
		frame.setVisible(true);
	}

	public static void main(String[] args) throws Exception {
		TicTacToe tic = new TicTacToe();
		tic.play();
	}

	public void play() throws Exception {
		try {
			var response = i.nextLine();
			var mark = response.charAt(8);
			var opponentMark = mark == 'x' ? 'o' : 'x';

			while (i.hasNextLine()) {
				response = i.nextLine();
				if (response.startsWith("VALID_MOVE")) {
					label.setText("Valid move, wait for your opponent.");
					currentButton.setFont(new Font("Arial", Font.BOLD, 30));
					currentButton.setForeground(Color.GREEN);
					currentButton.setText(Character.toString(mark));
				} else if (response.startsWith("OPPONENT_MOVED")) {
					var loc = Integer.parseInt(response.substring(15));
					buttonArr[loc].setFont(new Font("Arial", Font.BOLD, 30));
					buttonArr[loc].setForeground(Color.RED);
					buttonArr[loc].setText(Character.toString(opponentMark));
					label.setText("Your opponent has moved, now is your turn.");
				}

				else if (response.startsWith("VICTORY")) {
					JOptionPane.showMessageDialog(frame, "Congratulations. You Win.");
					break;
				} else if (response.startsWith("DEFEAT")) {
					JOptionPane.showMessageDialog(frame, "You lose.");
					break;
				} else if (response.startsWith("TIE")) {
					JOptionPane.showMessageDialog(frame, "Draw.");
					break;
				} else if (response.startsWith("OTHER_PLAYER_LEFT")) {
					JOptionPane.showMessageDialog(frame, "Game Ends. One of the players left.");
					break;
				}
			}
			o.println("QUIT");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			socket.close();
			frame.dispose();
		}
	}

	class SubmitBtn implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			label.setText("WELCOME " + text.getText());
			buttonSubmit.setEnabled(false);
			enabled = true;
			text.setEnabled(false);

			for (JButton b : buttonArr) {
				b.setEnabled(enabled);
			}
		}
	}

	class InstructionsBtn implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			JOptionPane.showMessageDialog(null, "Some information about the game.\n" + "Criteria for a valid move:\n"
					+ "-The move is not occupied by any mark.\n" + "-The move is made in the player's turn.\n"
					+ "-The move is made within the 3 x 3 board.\n"
					+ "The game would continue and switch among the opposite player until it reaches either one of the following conditions:\n"
					+ "-Player 1 wins.\n" + "-Player 2 wins.\n" + "-Draw.");
		}
	}

	class ExitBtn implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			System.exit(0);
		}
	}
}
