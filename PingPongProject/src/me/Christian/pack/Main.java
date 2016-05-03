package me.Christian.pack;


import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application{

	// Graphics "Engine"
	GraphicsContext gc;
	// width of the window and canvas
	public static int field_width = 600;
	// length of the window and canvas
	public static int field_length = 900;
	
	
	// distance of the players to the window border
	public static int player_windows_distance = 75;
	
	// The Ball
	public static Ball playball;
	
	// Size of the ball
	public static int PlayBallSize = 6;
	

	// Scores for Both Players
	public static int scorep1 = 0;
	public static int scorep2 = 0;

	// Movement for both Players
	public static boolean p1Up = false;
	public static boolean p2Up = false;
	public static boolean p1Down = false;
	public static boolean p2Down = false;

	// Seconds until the Game starts
	public static int startcountdown = 5;

	// To determine if the Game is over
	public static boolean GameIsOver = false;
	// Start loop and refresh loop
	public static Timeline tf, tf2;

	// To store the winner
	public static int winner = 0;

	// To determine if the KI of Player 1 or 2 is enabled
	public static boolean kip1 = true, kip2 = true;
	
	// Speed of the Game -- 4 seems fine
	public static int gamespeed = 4;
	
	// KI difficulty
	// 1 -> easy ; 2 -> hard ; 3 -> Impossible depending on the Player Size
	public static int KIdifficulty = 3;
	
	// Size of the Player
	public static int PlayerSize = 100;
	// Thickness of the Player
	public static int PlayerThickness = 20;
	
	// Wide of the middle, top and bottom walls
	public static int wallwide = 10;


	// Main...
	public static void main(String[] args) {
		launch(args);
	}

	// Called on launch(args) from Main
	@Override
	public void start(Stage primStage) throws Exception {
		Group root = new Group();
		Scene s = new Scene(root, field_length, field_width, Color.BLACK);

		// Create Game Area
		Canvas canvas = new Canvas(field_length, field_width);
		gc = canvas.getGraphicsContext2D();

		// Create Player 1
		Player p1 = new Player(player_windows_distance, field_width/3, PlayerThickness, PlayerSize, Color.BLUE);

		// Create Player 2
		Player p2 = new Player(field_length-player_windows_distance, field_width/3, PlayerThickness, PlayerSize, Color.RED);
		
		// Create the Ball x, y, size x, size y
		playball = new Ball((field_length/2)-PlayBallSize, field_width/2, PlayBallSize, PlayBallSize);

		// Store the Players into an Array so we can access them later
		Player.Players[0] = p1;
		Player.Players[1] = p2;

		// Set a Press Key Listener for the Player Movement
		// We need a Press and Release Listener because a Type Listener doesn't work on multiple Keys
		s.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent ke) {
				if (ke.getCode() == KeyCode.W) {
					// Tell the Game to move the Player up
					p1Up = true;
					// Disable the KI for Player 1
					kip1 = false;
				}else if(ke.getCode() == KeyCode.S){
					p1Down = true;
					kip1 = false;
				}else if(ke.getCode() == KeyCode.UP){
					p2Up = true;
					// Disable the KI for Player 2
					kip2 = false;
				}else if(ke.getCode() == KeyCode.DOWN){
					p2Down = true;
					kip2 = false;
				}
			}
		});

		// Set a Release Key Listener for the Player Movement
		s.setOnKeyReleased(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent ke) {
				if (ke.getCode() == KeyCode.W) {
					// Tell the Game to not move the Player up anymore
					p1Up = false;
				}else if(ke.getCode() == KeyCode.S){
					p1Down = false;
				}else if(ke.getCode() == KeyCode.UP){
					p2Up = false;
				}else if(ke.getCode() == KeyCode.DOWN){
					p2Down = false;
				}

				if (ke.getCode() == KeyCode.N) {
					if(GameIsOver){
						restart(gc);
					}
				}
			}
		});

		// Main Refresh Loop to draw the Game, check Collision and move the Players and the Ball
		tf = new Timeline(new KeyFrame(Duration.millis(gamespeed), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				// Move the Ball
				if(startcountdown < -1){
					playball.moveBall();
				}
				// Check Collision
				playball.CheckhitWall();
				// Draw Both Players, the ball and the Walls
				redraw(gc);


				// KI Player 2
				if(kip2){
					// If the ball is flying towards the Player and is already thru the middle
					if((playball.getY() < Player.Players[1].getY()+Player.Players[1].getSizeY()/2) && playball.gettoPlayer() == 1 && playball.getX() > field_length/2){
						// Tell the KI to go up
						Player.Players[1].goUpKI();
					}else if((playball.getY() > Player.Players[1].getY()-Player.Players[1].getSizeY()/2) && playball.gettoPlayer() == 1 && playball.getX() > field_length/2){
						Player.Players[1].goDownKI();
					}
				}

				// KI Player 1
				// same as Player 2
				if(kip1){
					if((playball.getY() < Player.Players[0].getY()+Player.Players[0].getSizeY()/2) && playball.gettoPlayer() == 0 && playball.getX() < field_length/2){
						Player.Players[0].goUpKI();
					}else if((playball.getY() > Player.Players[0].getY()-Player.Players[0].getSizeY()/2) && playball.gettoPlayer() == 0 && playball.getX() < field_length/2){
						Player.Players[0].goDownKI();
					}
				}
			}
		}));
		// Let it run Indefinite or until stop is issued
		tf.setCycleCount(Timeline.INDEFINITE);

		// Counter to start the Game
		tf2 = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				// Draw Timer and Info for Players
				drawStart(gc);
				// If the Timer
				if(startcountdown == -1){
					// Start the Game
					tf.play();
				}
			}
		}));
		// Tell the Timer to only do 7 Ticks
		tf2.setCycleCount(7);
		// Start the Game start Timer immediately
		tf2.play();


		// Add the Game Area to the Group root
		root.getChildren().add(canvas);

		// Clear the Game Area
		gc.clearRect(0, 0, field_length, field_width);

		// Set the Scene to the Primary Stage
		primStage.setScene(s);
		// Name the Window
		primStage.setTitle("Ping Pong Spiel");
		// Show the Window
		primStage.show();
	}

	// Method to restart the Game -- restart Variables and clear the Game Area
	public static void restart(GraphicsContext gc){
		gc.clearRect(0, 0, field_length, field_width);
		startcountdown = 5;
		scorep1 = 0;
		scorep2 = 0;
		Player.Players[0].setY(field_width/3);
		Player.Players[1].setY(field_width/2);
		GameIsOver = false;
		// launch start Timer again
		tf2.play();
	}

	
	// Draws the Start of the Game -- Timer & Info
	public static void drawStart(GraphicsContext gc){
		gc.clearRect(0, 0, field_length, field_width);
		gc.setFill(Color.LIME);
		gc.setFont(new Font((field_length*field_width)/6750));
		gc.fillText(String.valueOf(startcountdown) + "...", (field_length/2.25), field_width/2);
		gc.setFill(Color.GOLD);
		gc.setFont(new Font((field_length*field_width)/13500));
		gc.fillText("Controls: Player 1-> W/D | Player 2-> Up/Down", field_length/30, field_width/1.65);
		startcountdown--;
	}

	
	// Draw the Screen when a Player wins
	public static void drawFinalScreen(GraphicsContext gc){
		gc.clearRect(0, 0, field_length, field_width);
		tf.stop();
		if(winner == 1){
			gc.setFill(Color.BLUE);
			gc.setFont(new Font(50));
			gc.fillText("Player 1 won!!! ~ Press N to restart", 60, 280);
		}else if(winner == 2){
			gc.setFill(Color.RED);
			gc.setFont(new Font(50));
			gc.fillText("Player 2 won!!! ~ Press N to restart", 60, 280);
		}
	}

	// Method to draw the Game -- players, ball, walls
	public static void redraw(GraphicsContext gc){
		gc.clearRect(0, 0, field_length, field_width);
		for(int i = 0; i<Player.Players.length; i++){
			gc.setFill(Player.Players[i].getColor());
			gc.fillRect(Player.Players[i].getX(), Player.Players[i].getY(), Player.Players[i].getSizeX(), Player.Players[i].getSizeY());
		}


		gc.setFill(Color.GOLD);
		gc.fillRect(playball.getX(), playball.getY(), playball.getBallSizeX(), playball.getBallSizeY());
		

		gc.setStroke(Color.GRAY);
		gc.setLineDashes(10, 10, 10, 10, 10, 10);
		gc.setLineDashOffset(10);
		gc.setLineWidth(10);
		gc.strokeLine((field_length/2)-wallwide, -5, (field_length/2)-wallwide, field_width+wallwide);

		gc.setFill(Color.AZURE);
		gc.setFont(new Font(80));
		gc.fillText(String.valueOf(Main.scorep1), field_length/4.5, field_width/2);
		gc.fillText(String.valueOf(Main.scorep2), field_length/1.38, field_width/2);
		gc.setFont(new Font(30));
		if(kip1){
			gc.fillText("KI on\nPress W/S to join", field_length/6, field_width/1.5);
		}
		if(kip2){
			gc.fillText("KI on\nPress UP/DOWN to join", field_length/1.8, field_width/1.5);
		}
		gc.setFill(Color.BROWN);
		gc.fillRect(0, 0, field_length, wallwide);
		gc.fillRect(0, field_width-wallwide, field_length, wallwide);

		if(p1Up){
			Player.Players[0].goUp();
		}
		if(p2Up){
			Player.Players[1].goUp();
		}
		if(p1Down){
			Player.Players[0].goDown();
		}
		if(p2Down){
			Player.Players[1].goDown();
		}
		if(GameIsOver){
			tf.stop();
			drawFinalScreen(gc);
		}
	}
}
