package genesys;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Server{
	private static final char[] players = new char[] { 'X', 'O' };
	
	private Socket client1;
	private Socket client2;
	private DataOutputStream dos1;
	private DataOutputStream dos2;
	private DataInputStream dis1;
	private DataInputStream dis2;
	
	public void start(ConnectFive board) throws IOException{
		/**
		 * Start method
		 * Creating server socket
		 * instantiate input/output steams
		 * for loop which switches between clients, takes their input and checks if there is a winner
		 */
		int height = 6, width = 9, moves = height * width;
		ServerSocket ss;
		ss = new ServerSocket(9999);
		client1 = ss.accept();
		dis1 = new DataInputStream(client1.getInputStream()); 
		dos1 = new DataOutputStream(client1.getOutputStream());

		client2 = ss.accept();
		dis2 = new DataInputStream(client2.getInputStream()); 
		dos2 = new DataOutputStream(client2.getOutputStream());
		dos1.writeUTF("Use 0-" + (width - 1) + " to choose a column.");
		dos2.writeUTF("Use 0-" + (width - 1) + " to choose a column.");
		
		for (int player = 0; moves-- > 0; player = 1 - player) {
			char symbol = players[player];
			if(symbol == 'X') {  
				dos1.writeUTF(board.toString());
				board.chooseColumn(symbol, dis1, dos1);
			}else if(symbol == 'O') {
				dos2.writeUTF(board.toString());
				board.chooseColumn(symbol, dis2, dos2);
			}
			if (board.isWinningPlay()) {
				dos1.writeUTF("Use 0-" + (width - 1) + " to choose a column.");  
				dos1.writeUTF("Player " + symbol + " wins!");
				dos1.writeUTF("GG");
				dos2.writeUTF("Player " + symbol + " wins!");
				dos2.writeUTF("GG");
				return;
			}
		}
		dos1.writeUTF("Game over, no winner.");
		dos1.writeUTF("GG");
		dos2.writeUTF("Game over, no winner.");
		dos2.writeUTF("GG");
	}
	
	public static void main(String args[]) throws IOException {
		
		/**
		 * Instantiate ConnectFive board and starting the game
		 */
		int height = 6, width = 9;
        ConnectFive board = new ConnectFive(width, height);
        Server server = new Server();
        server.start(board);
        
	}
}

class ConnectFive {

    private final int width, height;
    private final char[][] grid;
    private int lastCol = -1, lastRow = -1;

    public ConnectFive(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new char[height][];
        for (int h = 0; h < height; h++) {
            Arrays.fill(this.grid[h] = new char[width], '.');
        }
    }

    public String toString() {
        return IntStream.range(0, this.width)
                        .mapToObj(Integer::toString)
                        .collect(Collectors.joining()) + "\n" +
               Arrays.stream(this.grid)
                     .map(String::new)
                     .collect(Collectors.joining("\n"));
    }

    /**
     * Ask user to choose column, repeating until a valid choice is made.
     * @throws IOException 
     */
    public void chooseColumn(char symbol, DataInputStream dis, DataOutputStream dos) throws IOException {
    	
            dos.writeUTF("\nPlayer " + symbol + " turn: ");
            String input = dis.readUTF();
            int col = Integer.parseInt(input);
            
            if (! (0 <= col && col < this.width)) {
            	dos.writeUTF("Column must be between 0 and " + (this.width - 1));
            }
            
            for (int h = this.height - 1; h >= 0; h--) {
                if (this.grid[h][col] == '.') {
                    this.grid[this.lastRow=h][this.lastCol=col] = symbol;
                    return;
                }
            }

            dos.writeUTF("\nPlayer " + symbol + " turn: ");
    }
   
    /**
     * Check if the last move played was a winning move.
     */
    public boolean isWinningPlay() {
        if (this.lastCol == -1) {
            throw new IllegalStateException("No move has been made yet");
        }
        char symbol = this.grid[this.lastRow][this.lastCol];
        String win = String.format("%c%c%c%c%c", symbol, symbol, symbol, symbol, symbol);
        return contains(this.horizontal(), win) ||
               contains(this.vertical(), win) ||
               contains(this.slashDiagonal(), win) ||
               contains(this.backslashDiagonal(), win);
    }

    /**
     * The contents of the row containing the last played chip.
     */
    private String horizontal() {
        return new String(this.grid[this.lastRow]);
    }

    /**
     * The contents of the column containing the last played chip.
     */
    private String vertical() {
        StringBuilder sb = new StringBuilder(this.height);
        for (int h = 0; h < this.height; h++) {
            sb.append(this.grid[h][this.lastCol]);
        }
        return sb.toString();
    }

    /**
     * The contents of the "/" diagonal containing the last played chip
     */
    private String slashDiagonal() {
        StringBuilder sb = new StringBuilder(this.height);
        for (int h = 0; h < this.height; h++) {
            int w = this.lastCol + this.lastRow - h;
            if (0 <= w && w < this.width) {
                sb.append(this.grid[h][w]);
            }
        }
        return sb.toString();
    }

    /**
     * The contents of the "\" diagonal containing the last played chip
     */
    private String backslashDiagonal() {
        StringBuilder sb = new StringBuilder(this.height);
        for (int h = 0; h < this.height; h++) {
            int w = this.lastCol - this.lastRow + h;
            if (0 <= w && w < this.width) {
                sb.append(this.grid[h][w]);
            }
        }
        return sb.toString();
    }

    private static boolean contains(String field, String value) {
        return field.indexOf(value) >= 0;
    }
}