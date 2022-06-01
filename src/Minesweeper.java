import java.util.ArrayList;
import java.util.logging.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.MouseInputListener;


/**
 * @author Alexander Fou
 * @version 1.0.0
 * Recreation of google minesweeper in Java.
 * One issue I could not fix: the game is over (I used a print statement to check, it has been logged
 * to console, and the timer has stopped) but the end game things like the restart button are not drawn.
 */
@SuppressWarnings("serial")
public class Minesweeper extends JFrame {
		
	private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	//all sprites used in this game
    private Image flagImage;
    private Image clockImage;
    private Image crossImage;
    private Image digInstructionImage;
    private Image flagInstructionImage;
    private Image tryAgainImage;
    private Image lossImage;
    private Image winImage;
    
	//width
	private static int xSize;
	//height
	private static int ySize;
	
	//number of clicks: first click has to be empty
	private static int numClicks = 0;
	
	//starting time: for timer, do current time minus start time to get elapsed time
	private long startTime;
	
	//field: contains all tiles with respecctive information
	private static tileType[][] field;
	//tile has been revealed?
	private boolean[][] revealed;
	//tile has been flagged?
	private boolean[][] flagged;
	
	//arraylist of bomb locations for defeat screen bomb reveal
	private ArrayList<Pair> bombLocations = new ArrayList<Pair>();
	
	//number of bombs in the grid
	private static int numBombs;
	//total number of tiles
	private static int numTiles;
	//number of tiles revealed
	private static int numTilesRevealed = 1;
	//number of flags placed
	private static int numFlags = 25;
	
	//ingame?
	private static gameState inGame;
	
	//time it takes to end the game
	private int finishTime;
	
	//high score time
	private int bestTime;
	
	//number of games played
	private int numGames;
	
	//mouse coords
	int mouseX, mouseY;
	
	//tile coords (of mouse)
	int tileX, tileY;
	
	
	//colors to use
	public static final Color LIGHTGREEN = new Color(162,209,73,255);
	public static final Color DARKGREEN = new Color(170,215,81,255);
	public static final Color REVEALEDLIGHTGREEN = new Color(229,194,159,255);
	public static final Color REVEALEDDARKGREEN = new Color(215,184,153,255);
	public static final Color MOUSEHIGHLIGHT = new Color(191,225,125,255);
	public static final Color GREENBAR = new Color(74, 117, 44, 255);
	
	//state of the game: either ingame, have won, or have lost
	public static enum gameState {
		game,
		victory,
		defeat
	}
	
	//type of tiles: bomb, empty, or number of bombs nearby
	public static enum tileType {
		bomb,
		empty,
		one,
		two,
		three,
		four,
		five,
		six,
		seven,
		eight;
		
		/**
		 * 
		 * @param numBombs number of bombs nearby
		 * @return appropriate tile
		 */
		public static tileType setValue (int numBombs) {
			switch (numBombs) {
			case 0:
				return tileType.empty;
			case 1:
				return tileType.one;
			case 2:
				return tileType.two;
			case 3:
				return tileType.three;
			case 4:
				return tileType.four;
			case 5: 
				return tileType.five;
			case 6:
				return tileType.six;
			case 7: 
				return tileType.seven;
			case 8:
				return tileType.eight;
			default:
				return tileType.empty;
			}
				
		}
		
		/**
		 * @return string version of tile
		 */
		@Override
		public String toString() {
			switch (this) {
			case bomb:
				return "bomb";
			case empty: 
				return "0";
			case one:
				return "1";
			case two:
				return "2";
			case three:
				return "3";
			case four:
				return "4";
			case five:
				return "5";
			case six:
				return "6";
			case seven:
				return "7";
			case eight:
				return "8";
			default:
				return "";
			}
		}
	}
	
	/**
	 * Initialize a Minesweeper object with grid size and number of bombs
	 * @param xSize width of minesweeper grid
	 * @param ySize height of minesweeper grid
	 * @param numBombs the number of bombs in the grid: default is 25
	 */
	public Minesweeper(int xSize, int ySize, int numBombs) {
		inGame = gameState.game;
		try {
			//load all images/audio clips
			//System.out.println("tryna lad images");
			this.flagImage = new ImageIcon(Minesweeper.class.getResource("flag.png")).getImage();
			this.clockImage = new ImageIcon(Minesweeper.class.getResource("clock.png")).getImage();
			this.crossImage = new ImageIcon(Minesweeper.class.getResource("cross.png")).getImage();
			this.digInstructionImage = new ImageIcon(Minesweeper.class.getResource("dig.png")).getImage();
			this.flagInstructionImage = new ImageIcon(Minesweeper.class.getResource("flagInstruction.png")).getImage();
			this.tryAgainImage = new ImageIcon(Minesweeper.class.getResource("tryAgainButton.png")).getImage();
			this.lossImage = new ImageIcon(Minesweeper.class.getResource("lose.png")).getImage();
			this.winImage = new ImageIcon(Minesweeper.class.getResource("win.png")).getImage();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//initialize instance variables
		logger.log(Level.INFO, "Initializing Minesweeper object");
		Minesweeper.xSize = xSize;
		Minesweeper.ySize = ySize;
		Minesweeper.numBombs = numBombs;
		
		//calculate number of tiles
		int totalGridSize = xSize * ySize;
		Minesweeper.numTiles = totalGridSize - numBombs;
		revealed = new boolean[xSize][ySize];
		flagged = new boolean[xSize][ySize];
		field = new tileType[xSize][ySize];
		
		//window stuff
		this.setTitle("Minesweeper");
    	this.setSize(1296,829);
    	this.setIconImage(flagImage);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.setResizable(false);
        
        //add listeners
        Board board = new Board();
        this.setContentPane(board);
        
        Move move = new Move();
        this.addMouseMotionListener(move);
     
        Click click = new Click();
        this.addMouseListener(click);
        //end listeners
		
		logger.log(Level.INFO, "Finished initialization");
	}
	/**
	 * resets grid: used together with the restart button in mouseClicked method
	 */
	public void resetGame() {
		numFlags = numBombs;
		inGame = gameState.game;
		numTilesRevealed = 1;
		numClicks = 0;
		bombLocations.clear();
		for (int i = 0; i < xSize; i++) {
			for (int k = 0; k < ySize; k++) {
				revealed[i][k] = false;
				flagged[i][k] = false;
				field[i][k] = null;
			}
		}
	}
	
	/**
	 * Create the minesweeper grid with the correct number of bombs: first click must be empty
	 * @param numBombs number of bombs in the grid
	 * @param x x coordinate of click
	 * @param y ycoordinate of click
	 */
	public void constructGrid(int numBombs, int x, int y) {
		logger.log(Level.INFO, "Constructing grid");
		//place bombs
		int currentNumBombs = 0;
		
		//first tile clicked must be an empty tile
		numTilesRevealed++;
		field[x][y] = tileType.empty;
		while (currentNumBombs < numBombs) {
			int xPos = (int) (Math.random() * xSize);
			int yPos = (int) (Math.random() * ySize);
			//check to see if it is already a bomb or if it is near the starting tile
			if (field[xPos][yPos] == tileType.bomb) { 
				continue;
			} else if  (Math.abs(x-xPos) <= 1 && Math.abs(y-yPos) <= 1) {
				continue;
			}
			else {
				field[xPos][yPos] = tileType.bomb;
				currentNumBombs++;
			}
			
		}
		
		//check each tile for surrounding bombs, update accordingly
		for (int i = 0; i < xSize; i++) {
			for (int k = 0; k < ySize; k++) {
				//not revealed
				revealed[i][k] = false;
				flagged[i][k] = false;
				if (field[i][k] == tileType.bomb) {
					bombLocations.add(new Pair(i, k));
					continue; // skip over bombs
				}
				
				int adjacentBombCount = 0;
				//check adjacent squares: iterate through all the squares around it, check for bombs, set number accordingly
				for (int x1 = -1; x1 <= 1; x1++) {
					for (int y1 = -1; y1 <= 1; y1++) {
						if (i + x1 >= 0 && i + x1 < xSize && k + y1 >= 0 && k + y1 < ySize) {
							if (field[i+x1][k+y1] == tileType.bomb) {
								adjacentBombCount++;
							}
						}
					}
				}
				
				field[i][k] = tileType.setValue(adjacentBombCount);
			}
		
		}
		numFlags = numBombs;
		logger.log(Level.INFO, "Done constructing grid");
	}
	/**
	 * @return number of bombs
	 */
	public int getNumBombs() {
		return numBombs;
	}
	
	/**
	 * @return number of tiles
	 */
	public int getNumTiles() {
		return numTiles;
	}
	
	/**
	 * @return number of tiles that are revealed
	 */
	public int getNumTilesRevealed() {
		return numTilesRevealed;
	}
	
	/**
	 * game end conditions
	 * @param endType either "win" or "loss" depending on how the game ends
	 */
	public void endGame(String endType) {
		finishTime = (int) (System.currentTimeMillis() - startTime) / 1000;
		switch (endType) {
		case "loss":
			inGame = gameState.defeat;
			numGames++;
			logger.log(Level.SEVERE, "YOU LOST!");
			for (int i = 0; i < bombLocations.size(); i++) { //reveal all bombs
				revealed[bombLocations.get(i).x][bombLocations.get(i).y] = true;
			}
			break;
		case "win":
			inGame = gameState.victory;
			numGames++;
			if (numGames == 1) bestTime = finishTime; //if first win, is automatically best time
			if (finishTime < bestTime) bestTime = finishTime; //get best time (of current session)
			logger.log(Level.WARNING, "YOU WON!");
			break;
		}
	}
	
	/**
	 * checks to see if click has ended game or not
	 * @param x the x coordinate of the click (grid)
	 * @param y the y coordinate of the click (grid)
	 */
	public void checkWin(int x, int y) {
		if (revealed[x][y] == true && field[x][y] == tileType.bomb) endGame("loss"); //dug mine?
		if (getNumTilesRevealed() == getNumTiles()) endGame("win"); //dug all tiles?
	}
	
	/**
	 * checks to see if game is over: needed to fix bug where game doesn't end when all tiles are revealed
	 */
	public void checkWin() {
		//System.out.println("checked win");
		int rev = 0;
		for (Pair p : bombLocations) { //see if bomb is revealed: more efficient than nested for loop
			if (revealed[p.x][p.y] == true && field[p.x][p.y] == tileType.bomb) endGame("loss");
		}
		for (int i = 0; i < xSize; i++) {
			for (int k = 0; k < ySize; k++) {
				if (revealed[i][k]) rev++;
			}
		}
		if (rev == numTiles) {
			endGame("win");
		}
		//System.out.println(rev + " " + numTiles);
	}
	
	
	/**
	 * method to reveal all nearby tiles if a tile is empty: must be recursive in case there are empty tiles around another empty tile
	 * @param x the x coordinate of click
	 * @param y the y coordinate of click
	 */
	public void exposeNearbyTiles(int x, int y) {
		//logger.log(Level.INFO, "recursively searched for nearby empty tiles...");
		//get all tiles within 1 tile of the click
		for (int i = -1; i <= 1; i++) {
			for (int k = -1; k <= 1; k++) {
				if (i + x >= 0 && i + x < xSize && k + y >= 0 && k + y < ySize && revealed[x+i][y+k] == false) {
					revealed[x+i][y+k] = true;
					if (field[x+i][y+k] == tileType.empty) { //recursive? needs to make sure it is not revealed (prevent infinite recursion)
						exposeNearbyTiles(x+i,y+k);
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {
		Minesweeper m = new Minesweeper(16,9,25);
		while (true) {
			m.repaint();
		}
	}
	
	public class Board extends JPanel {
		
		/**
		 * paint a singular square: helper method to create checkerboard pattern
		 * @param color color of the square: taken from checkerboardColor method
		 * @param g graphics
		 * @param i x coordinate of square
		 * @param k y coordinate of square
		 */
		private void paintSquare(Color color, Graphics g, int i, int k) {
			g.setColor(color);
			g.fillRect(i * 80, k * 80 + 80, 80, 80);
		}
		
		/**
		 * main paint method
		 * @param g graphics
		 */
    	public void paintComponent(Graphics g) {
    		//create green bar: the rest will be drawn over
    		g.setColor(GREENBAR);
    		g.fillRect(0,0,1300,900);
    		
    		//create tiles with highlight: follow checkerboard
    		for (int i = 0; i < xSize; i++) {
    			for (int k = 0; k < ySize; k++) {
    				if (i % 2 == 0) {
    					if (k % 2 == 0) {
    						g.setColor(LIGHTGREEN);
    					} else {
    						g.setColor(DARKGREEN);
    					}
    				} else {
    					if (k % 2 == 0) {
    						g.setColor(DARKGREEN);
    					} else {
    						g.setColor(LIGHTGREEN);
    					}
    				}

    				//flag tile?
    				if (flagged[i][k] == true) {
    					paintSquare(checkerboardColor(i, k, false), g, i, k);
    					g.drawImage(flagImage, i * 80 + 15, k * 80 + 90, 60, 60, null);
    					continue;
    				}
    				
    				//bomb tile? random color...
    				if (revealed[i][k] == true && field[i][k] == tileType.bomb) {
    					paintSquare(new Color(i * 1000 % 255, k * 1000 % 255, i * k * 100 % 255), g, i, k);
    					continue;
    				}
    				
    				//not a flag nor a bomb? must be regular tile...
    				else if (revealed[i][k] == true && field[i][k] != tileType.bomb) {
    					g.setColor(checkerboardColor(i,k,true));
    					g.fillRect(i * 80, k * 80 + 80, 80, 80);
    					
    					//handles the numbers
    					g.setColor(Color.black);
    					Font font = new Font("Arial", Font.BOLD, 40);
    					g.setFont(font);
    					if (field[i][k].toString().equals("0")) {
    						g.drawString("", i * 80 + 80, k * 80 + 80 + 55);
    						continue;
    					} else {
    						switch (field[i][k].toString()) {
    						case "1":
    							g.setColor(Color.blue); break;
    						case "2":
    							g.setColor(Color.green); break;
    						case "3":
    							g.setColor(Color.red); break;
    						case "4":
    							g.setColor(Color.magenta); break;
    						case "5":
    							g.setColor(Color.yellow); break;
    						case "6":
    							g.setColor(Color.cyan); break;
    						case "7":
    							g.setColor(Color.black); break;
    						case "8":
    							g.setColor(Color.gray); break;
    						}
    					g.drawString(field[i][k].toString(), i * 80 + 27, k * 80 + 80 + 55);
    					continue;
    					}
    					//end of number handling
    				}
    				
    				//mouse highlight
    				if (mouseX >=  i * 80 && mouseX < i * 80 + 80 && mouseY >= k * 80 + 80 + 26 && mouseY < k * 80 + 80 + 80 + 26) {
    					g.setColor(MOUSEHIGHLIGHT);
    				} else { // checkerboardcolor
    					g.setColor(checkerboardColor(i,k, false));
    				}
    				
    				g.fillRect(i * 80, k * 80 + 80, 80, 80);
    				
    				//draws flag and timer in upper middle
    				drawFlagNumber(numFlags, g);
    				drawTimer(g);
    				
    				if (numClicks == 0) { //first click? show instructions
    					g.drawImage(flagInstructionImage, 300, 350, 300, 240, null);
    					g.drawImage(digInstructionImage, 570, 350, 160, 160, null);
    				}
    				
    				//end game things: win/loss icon, try again button
    				/*
    				 * there is a bug where the game is won/lost but the endgame stuff is not displayed.
    				 * I used print statements to check and saw that they were printed along with the
    				 * console logging stuff (in the endGame() method) but noticed that the restart button
    				 * and win/loss images were not displayed. Not sure how to fix...
    				 */
    				if (inGame != gameState.game) {
    					g.drawImage(tryAgainImage,490, 460, 300, 80, null);
    				}
    				
    				if (inGame == gameState.victory) {
    					//System.out.println("You won!");
    					g.drawImage(winImage, 595, 360, 90, 90, null);
    					g.setColor(Color.BLACK);
    					g.drawString("You won!", 560, 350);
    				}
    				if (inGame == gameState.defeat) {
    					//System.out.println("You lost!");
    					for (int i1 = 0; i1 < xSize; i1++) {
    						for (int k1 = 0; k1 < ySize; k1++) {
    							if (flagged[i1][k1] && field[i1][k1] != tileType.bomb) { //incorrect flag?
    								g.drawImage(crossImage, i1 * 80, k1 * 80 + 80, 80, 80, null);
    							}
    						}
    					}
    					g.drawImage(lossImage, 595, 360, 90, 90, null);
    					g.setColor(Color.BLACK);
    					g.drawString("You lost!", 560, 350);
    					
    				}
    				
    				//end of end game things
    			}
    		}
    		
    	}

    	/**\
    	 * helper method to draw timer
    	 * @param g graphics
    	 */
    	private void drawTimer(Graphics g) {
    		if (clockImage != null) {
    			g.drawImage(clockImage,600, 10, 120, 120, null); //clock
    		}
    		//handles the actual timer
    		g.setColor(Color.white);
    		g.setFont(new Font("Arial", Font.BOLD, 40));
    		
    		if (numClicks > 0 && inGame == gameState.game) {
    			g.drawString("" + (long) ((System.currentTimeMillis() - startTime) / 1000.0), 750, 50);
    		} else if (inGame != gameState.game) {
    			g.drawString("" + finishTime, 750, 50);
    		} else {
    			g.drawString("0", 750, 50);
    		}
    		//end of actual timer
    	}
    	
    	/**
    	 * draws the flag count
    	 * @param numberFlags number of flags left
    	 * @param g graphics
    	 */
    	private void drawFlagNumber(int numberFlags, Graphics g) {
    		if (flagImage != null) {
    			g.drawImage(flagImage, 500, 10, 60, 60, null);
    		}
    		g.setColor(Color.white);
    		g.setFont(new Font("Arial", Font.BOLD, 40));
    		g.drawString("" + numberFlags, 560, 50);
    	}
    	
    	/**
    	 * calculates what color for the checkerboard pattern, used together with paintSquare method
    	 * @param i x coordinate of tile
    	 * @param k y coordinate of tile
    	 * @param revealed has it been revealed yet?
    	 * @return color object 
    	 */
    	private Color checkerboardColor(int i, int k, boolean revealed) {
    		if (revealed) {
	    		if (i % 2 == 0) {
					if (k % 2 == 0) {
						return REVEALEDLIGHTGREEN;
					} else {
						return REVEALEDDARKGREEN;
					}
				} else {
					if (k % 2 == 0) {
						return REVEALEDDARKGREEN;
					} else {
						return REVEALEDLIGHTGREEN;
					}
				}
    		} else {
    			if (i % 2 == 0) {
					if (k % 2 == 0) {
						return LIGHTGREEN;
					} else {
						return DARKGREEN;
					}
				} else {
					if (k % 2 == 0) {
						return DARKGREEN;
					} else {
						return LIGHTGREEN;
					}
				}
    		}
    	}
    }
    
	
	//move libary
    public class Move implements MouseInputListener {
    	
		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		//update Minesweeper mouseX and mouseY for our own calculations
		public void mouseMoved(MouseEvent e) {
			//System.out.println("moved" + Board.mouseX + " " + Board.mouseY);
			mouseX = e.getX();
			mouseY = e.getY();
		}
    		
    }
    
    public class Click implements MouseInputListener {
    
		@Override
		public void mouseClicked(MouseEvent e) {
			checkWin();
			//create variables to avoid calling method repeatedly
			int x = getBoxX();
			int y = getBoxY();
			int clickType = e.getButton();
			
			
			//ingame and valid click?
			if (x != -1 && y != -1 && inGame == gameState.game) {
				//System.out.println(x + " " + y);
				switch (clickType) {
				case 1: //left click
					if (flagged[x][y] == true) break; //don't  dig flags
					checkWin(x,y);
						if (numClicks == 0) { //first click?
							startTime = System.currentTimeMillis(); //get current time to start timer
							constructGrid(numBombs, x, y); //create grid
						}
						revealed[x][y] = true;
						if (field[x][y] == tileType.empty) exposeNearbyTiles(x,y); //reveal nearby tiles around empty tiles
						numClicks++;
					break;	
				case 2://middle click: reveal all nearby tiles if flags are satisfied
					if (revealed[x][y] == true && field[x][y] != tileType.bomb && flagged[x][y] == false) { //pre-check
						int numFlagsNearby = 0;
						//get number of nearby flags
						for (int i = -1; i <= 1; i++) {
							for (int k = -1; k <= 1; k++) {
								if (i + x >= 0 && i + x < xSize && k + y >= 0 && k + y < ySize) {
									if (flagged[i+x][y+k] == true) {
										numFlagsNearby++;
									}
								}
							}
						}
						if (field[x][y].toString().equals((numFlagsNearby + ""))) { //if flags are satisfied...
							checkWin(x,y);
							for (int i = -1; i <= 1; i++) {
								for (int k = -1; k <= 1; k++) {
									if (i + x >= 0 && i + x < xSize && k + y >= 0 && k + y < ySize) {
										if (flagged[i+x][y+k] == false && revealed[i+x][y+k] == false) {
											revealed[i+x][y+k] = true;
											checkWin(i+x,y+k);
											if (field[i+x][y+k] == tileType.empty) {
												exposeNearbyTiles(i+x,y+k);
											}
										}
									}
								}
							}
						}
					}
					break;
				case 3: //right click
					if (revealed[x][y] == false) {
						//toggle flag
						if (flagged[x][y]) {
							numFlags++;
						} else {
							numFlags--;
						}
						flagged[x][y] = !flagged[x][y];
					}
					break;
				}
				if (numClicks == 0) { // startthe timer
					startTime = System.currentTimeMillis();
				}
			} else {
				logger.log(Level.WARNING, "Not a valid box!");
			}
			
			numTilesRevealed = 0;
			for (boolean[] a : revealed) { //update number of tiles revealed
				for (boolean b : a) {
					if (b) numTilesRevealed++;
				}
				
			//System.out.println(numTilesRevealed + " num tiles total: " + numTiles);
			}
			
			if (inGame != gameState.game) { //restart button
				if (mouseX >= 490 && mouseX <= 790 && mouseY >= 460 && mouseY <= 540) {
					resetGame();
				}
			}
			
			checkWin();//needed to fix bug where game doesn't end
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			// TODO Auto-generated method stub
			mouseX = e.getX();
			mouseY = e.getY();
		}
    	
		/**
		 * @return grid coordinate in X axis
		 */
    	public int getBoxX() {
    		for (int i = 0; i < xSize; i++) {
    			for (int k = 0; k < ySize; k++) {
    				if (mouseX >= i * 80 && mouseX < i * 80 + 80 && mouseY >= k * 80 + 80 + 26 && mouseY < k * 80 + 80 + 80 + 26) {
    					return i;
    				}
    			}
    		}
    		return -1;
    	}
    	
    	/**
    	 * @return grid coordinate in y axis
    	 */
    	public int getBoxY() {
    		for (int i = 0; i < xSize; i++) {
    			for (int k = 0; k < ySize; k++) {
    				if (mouseX >= i * 80 && mouseX < i * 80 + 80 && mouseY >= k * 80 + 80 + 26 && mouseY <  k * 80 + 80 + 80 + 26) {
    					return k;
    				}
    			}
    		}
    		return -1;
    	}
    	
    }

    /**
     * helper class to store bomb locations: x,y pairs (x, y coordinates)
     */
    class Pair { //implements Comparable<Pair> {
    	public int x;
    	public int y;

    	/**
    	 * @param x the x coordinate of the bomb
    	 * @param y the y coordinate of the bomb
    	 */
    	public Pair(int x, int y) {
    		this.x = x;
    		this.y = y;
    	}
    	

    	
    	
    }
    
}