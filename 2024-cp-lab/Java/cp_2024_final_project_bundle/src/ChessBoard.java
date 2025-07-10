// 양정욱_2023-19674
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
import javax.swing.*;
import javax.swing.border.*;
//======================================================Don't modify below===============================================================//
enum PieceType {king, queen, bishop, knight, rook, pawn, none}
enum PlayerColor {black, white, none}
	
public class ChessBoard {
	private final JPanel gui = new JPanel(new BorderLayout(3, 3));
	private JPanel chessBoard;
	private JButton[][] chessBoardSquares = new JButton[8][8];
	private Piece[][] chessBoardStatus = new Piece[8][8];
	private ImageIcon[] pieceImage_b = new ImageIcon[7];
	private ImageIcon[] pieceImage_w = new ImageIcon[7];
	private JLabel message = new JLabel("Enter Reset to Start");

	ChessBoard(){
		initPieceImages();
		initBoardStatus();
		initializeGui();
	}
	
	public final void initBoardStatus(){
		for(int i=0;i<8;i++){
			for(int j=0;j<8;j++) chessBoardStatus[j][i] = new Piece();
		}
	}
	
	public final void initPieceImages(){
		pieceImage_b[0] = new ImageIcon(new ImageIcon("./img/king_b.png").getImage().getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH));
		pieceImage_b[1] = new ImageIcon(new ImageIcon("./img/queen_b.png").getImage().getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH));
		pieceImage_b[2] = new ImageIcon(new ImageIcon("./img/bishop_b.png").getImage().getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH));
		pieceImage_b[3] = new ImageIcon(new ImageIcon("./img/knight_b.png").getImage().getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH));
		pieceImage_b[4] = new ImageIcon(new ImageIcon("./img/rook_b.png").getImage().getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH));
		pieceImage_b[5] = new ImageIcon(new ImageIcon("./img/pawn_b.png").getImage().getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH));
		pieceImage_b[6] = new ImageIcon(new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB));
		
		pieceImage_w[0] = new ImageIcon(new ImageIcon("./img/king_w.png").getImage().getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH));
		pieceImage_w[1] = new ImageIcon(new ImageIcon("./img/queen_w.png").getImage().getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH));
		pieceImage_w[2] = new ImageIcon(new ImageIcon("./img/bishop_w.png").getImage().getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH));
		pieceImage_w[3] = new ImageIcon(new ImageIcon("./img/knight_w.png").getImage().getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH));
		pieceImage_w[4] = new ImageIcon(new ImageIcon("./img/rook_w.png").getImage().getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH));
		pieceImage_w[5] = new ImageIcon(new ImageIcon("./img/pawn_w.png").getImage().getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH));
		pieceImage_w[6] = new ImageIcon(new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB));
	}
	
	public ImageIcon getImageIcon(Piece piece){
		if(piece.color.equals(PlayerColor.black)){
			if(piece.type.equals(PieceType.king)) return pieceImage_b[0];
			else if(piece.type.equals(PieceType.queen)) return pieceImage_b[1];
			else if(piece.type.equals(PieceType.bishop)) return pieceImage_b[2];
			else if(piece.type.equals(PieceType.knight)) return pieceImage_b[3];
			else if(piece.type.equals(PieceType.rook)) return pieceImage_b[4];
			else if(piece.type.equals(PieceType.pawn)) return pieceImage_b[5];
			else return pieceImage_b[6];
		}
		else if(piece.color.equals(PlayerColor.white)){
			if(piece.type.equals(PieceType.king)) return pieceImage_w[0];
			else if(piece.type.equals(PieceType.queen)) return pieceImage_w[1];
			else if(piece.type.equals(PieceType.bishop)) return pieceImage_w[2];
			else if(piece.type.equals(PieceType.knight)) return pieceImage_w[3];
			else if(piece.type.equals(PieceType.rook)) return pieceImage_w[4];
			else if(piece.type.equals(PieceType.pawn)) return pieceImage_w[5];
			else return pieceImage_w[6];
		}
		else return pieceImage_w[6];
	}

	public final void initializeGui(){
		gui.setBorder(new EmptyBorder(5, 5, 5, 5));
	    JToolBar tools = new JToolBar();
	    tools.setFloatable(false);
	    gui.add(tools, BorderLayout.PAGE_START);
	    JButton startButton = new JButton("Reset");
	    startButton.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent e){
	    		initiateBoard();
	    	}
	    });
	    
	    tools.add(startButton);
	    tools.addSeparator();
	    tools.add(message);

	    chessBoard = new JPanel(new GridLayout(0, 8));
	    chessBoard.setBorder(new LineBorder(Color.BLACK));
	    gui.add(chessBoard);
	    ImageIcon defaultIcon = new ImageIcon(new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB));
	    Insets buttonMargin = new Insets(0,0,0,0);
	    for(int i=0; i<chessBoardSquares.length; i++) {
	        for (int j = 0; j < chessBoardSquares[i].length; j++) {
	        	JButton b = new JButton();
	        	b.addActionListener(new ButtonListener(i, j));
	            b.setMargin(buttonMargin);
	            b.setIcon(defaultIcon);
	            if((j % 2 == 1 && i % 2 == 1)|| (j % 2 == 0 && i % 2 == 0)) b.setBackground(Color.WHITE);
	            else b.setBackground(Color.gray);
	            b.setOpaque(true);
	            b.setBorderPainted(false);
	            chessBoardSquares[j][i] = b;
	        }
	    }

	    for (int i=0; i < 8; i++) {
	        for (int j=0; j < 8; j++) chessBoard.add(chessBoardSquares[j][i]);
	        
	    }
	}

	public final JComponent getGui() {
	    return gui;
	}
	
	public static void main(String[] args) {
	    Runnable r = new Runnable() {
	        @Override
	        public void run() {
	        	ChessBoard cb = new ChessBoard();
                JFrame f = new JFrame("Chess");
                f.add(cb.getGui());
                f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                f.setLocationByPlatform(true);
                f.setResizable(false);
                f.pack();
                f.setMinimumSize(f.getSize());
                f.setVisible(true);
            }
        };
        SwingUtilities.invokeLater(r);
	}
		
			//================================Utilize these functions========================================//	
	
	class Piece{
		PlayerColor color;
		PieceType type;
		
		Piece(){
			color = PlayerColor.none;
			type = PieceType.none;
		}
		Piece(PlayerColor color, PieceType type){
			this.color = color;
			this.type = type;
		}
	}
	
	public void setIcon(int x, int y, Piece piece){
		chessBoardSquares[y][x].setIcon(getImageIcon(piece));
		chessBoardStatus[y][x] = piece;
	}
	
	public Piece getIcon(int x, int y){
		return chessBoardStatus[y][x];
	}
	
	public void markPosition(int x, int y){
		chessBoardSquares[y][x].setBackground(Color.pink);
	}
	
	public void unmarkPosition(int x, int y){
		if((y % 2 == 1 && x % 2 == 1)|| (y % 2 == 0 && x % 2 == 0)) chessBoardSquares[y][x].setBackground(Color.WHITE);
		else chessBoardSquares[y][x].setBackground(Color.gray);
	}
	
	public void setStatus(String inpt){
		message.setText(inpt);
	}
	
	public void initiateBoard(){
		for(int i=0;i<8;i++){
			for(int j=0;j<8;j++) setIcon(i, j, new Piece());
		}
		setIcon(0, 0, new Piece(PlayerColor.black, PieceType.rook));
		setIcon(0, 1, new Piece(PlayerColor.black, PieceType.knight));
		setIcon(0, 2, new Piece(PlayerColor.black, PieceType.bishop));
		setIcon(0, 3, new Piece(PlayerColor.black, PieceType.queen));
		setIcon(0, 4, new Piece(PlayerColor.black, PieceType.king));
		setIcon(0, 5, new Piece(PlayerColor.black, PieceType.bishop));
		setIcon(0, 6, new Piece(PlayerColor.black, PieceType.knight));
		setIcon(0, 7, new Piece(PlayerColor.black, PieceType.rook));
		for(int i=0;i<8;i++){
			setIcon(1, i, new Piece(PlayerColor.black, PieceType.pawn));
			setIcon(6, i, new Piece(PlayerColor.white, PieceType.pawn));
		}
		setIcon(7, 0, new Piece(PlayerColor.white, PieceType.rook));
		setIcon(7, 1, new Piece(PlayerColor.white, PieceType.knight));
		setIcon(7, 2, new Piece(PlayerColor.white, PieceType.bishop));
		setIcon(7, 3, new Piece(PlayerColor.white, PieceType.queen));
		setIcon(7, 4, new Piece(PlayerColor.white, PieceType.king));
		setIcon(7, 5, new Piece(PlayerColor.white, PieceType.bishop));
		setIcon(7, 6, new Piece(PlayerColor.white, PieceType.knight));
		setIcon(7, 7, new Piece(PlayerColor.white, PieceType.rook));
		for(int i=0;i<8;i++){
			for(int j=0;j<8;j++) unmarkPosition(i, j);
		}
		onInitiateBoard();
	}
//======================================================Don't modify above==============================================================//	




//======================================================Implement below=================================================================//		
	private Special SpecialState[][]=new Special[8][8];
	public Special getSpecial(int x, int y){
		if(0<=x&&x<8&&0<=y&&y<8) return SpecialState[y][x];
		else return new Special();
	}
	public void setCheck(int x, int y){
		getSpecial(x,y).setCheck(true);
	}

	class Special {
		private boolean check;
		private boolean enpassant;
		private boolean castling;
		Special(){
			this.enpassant=false;
			this.castling=false;
		}

		void setEnpassant(boolean a){
			this.enpassant=a;
		};
		void setCastling(boolean a){
			this.castling=a;
		};
		void setCheck(boolean a){
			this.check=a;
		}


	}
	enum MagicType {NORMAL, CHECK, CHECKMATE};
	private int selX, selY;
	private boolean check, checkmate, end;

	private Piece prevSelPiece;
	private int prevSelx,prevSely;
	private boolean isCheckState(PlayerColor turn){
		findCheckSquare(turn);
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++){
				//check인지 확인
				if(getIcon(i,j).color==turn&&getIcon(i,j).type==PieceType.king&&getSpecial(i,j).check) {
					return true;
				}

			}
		}
		return false;
	}
	private boolean isCheck(int x, int y){
		return (0<=x&&x<8&&0<=y&&y<8)?getSpecial(x,y).check: false;
	}
	private boolean isBlocked(int x, int y, PlayerColor turn){
		if(turn==PlayerColor.white){
			return (0<=x&&x<8&&0<=y&&y<8)?(isCheck(x,y)||isWhite(x,y)): true;
		} else if(turn==PlayerColor.black){
			return (0<=x&&x<8&&0<=y&&y<8)?isCheck(x,y)||isBlack(x,y): true;
		} else return false;
	}
	private boolean isCheckmateState(PlayerColor turn){
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++){
				if(getIcon(i,j).color==turn&&getIcon(i,j).type==PieceType.king) {
					if(isCheckmate(i,j,turn)){
						for(int x=0;x<8;x++){
							for(int y=0;y<8;y++){
								if(getIcon(x,y).color==turn){
									validMoveInCheck(getIcon(x,y),x,y);
									for(int n=0;n<8;n++){
										for(int m=0;m<8;m++){
											if(chessBoardSquares[n][m].getBackground()==Color.pink){
												return false;
											}
										}
									}
								}
							}
						}
						return true;
					}
				}

			}
		}
		return false;
	}
	private boolean isCheckmate(int x, int y, PlayerColor turn){
		if(isCheck(x,y)&&isBlocked(x,y+1, turn)&&isBlocked(x,y-1, turn)&&isBlocked(x+1,y+1, turn)&&isBlocked(x+1,y, turn)&&isBlocked(x+1,y-1, turn)&&isBlocked(x-1,y+1, turn)&&isBlocked(x-1,y, turn)&&isBlocked(x-1,y-1, turn)) return true;
		else return false;
	}
	private void validMoveInCheckTest(int prevSelx, int prevSely, int x, int y, Piece selPiece){
		Piece temp;
		temp=getIcon(x,y);
		setIcon(x, y, selPiece);
		setIcon(prevSelx, prevSely, new Piece());
		if(!isCheckState(selPiece.color)) markPosition(x, y);
		setIcon(x , y, temp);
		setIcon(prevSelx, prevSely, selPiece);
	}
	private boolean isEmpty(int x, int y){
		return (0<=x&&x<8&&0<=y&&y<8)?chessBoardStatus[y][x].type == PieceType.none: false;
	}
	private boolean isBlack(int x, int y){
		return (0<=x&&x<8&&0<=y&&y<8)?chessBoardStatus[y][x].color == PlayerColor.black: false;
	}
	private boolean isWhite(int x, int y){
		return (0<=x&&x<8&&0<=y&&y<8)?chessBoardStatus[y][x].color == PlayerColor.white:false;
	}
	private void changeTurn(){
		if(turn==PlayerColor.black) turn=PlayerColor.white;
		else turn=PlayerColor.black;
	}
	private void findCheckSquare(PlayerColor turn) {
		for(int x=0;x<8;x++){
			for(int y=0;y<8;y++){
				getSpecial(x,y).setCheck(false);
			}
		}
		for(int x=0;x<8;x++){
			for(int y=0;y<8;y++){
				if(getIcon(x,y).color!=turn&&getIcon(x,y).color!=PlayerColor.none){
					PieceType type = getIcon(x, y).type;
					PlayerColor color = getIcon(x, y).color;
					{
						switch (type) {
							case PieceType.pawn: {
								if (color == PlayerColor.white) {
									if (isBlack(x - 1, y - 1)||isWhite(x - 1, y - 1)) setCheck(x - 1, y - 1);
									if (isBlack(x - 1, y + 1)||isWhite(x - 1, y + 1)) setCheck(x - 1, y + 1);
								} else if (color == PlayerColor.black) {
									if (isWhite(x + 1, y - 1)||isBlack(x+1, y-1)) setCheck(x + 1, y - 1);
									if (isWhite(x + 1, y + 1)||isBlack(x+1,y+1)) setCheck(x + 1, y + 1);
								}
							}
							break;
							case PieceType.knight: {
								if (color == PlayerColor.white) {
									if (isWhite(x + 2, y - 1) || isBlack(x + 2, y - 1) || isEmpty(x + 2, y - 1)) setCheck(x + 2, y - 1);
									if (isWhite(x + 2, y + 1) || isBlack(x + 2, y + 1) || isEmpty(x + 2, y + 1)) setCheck(x + 2, y + 1);
									if (isWhite(x - 2, y - 1) || isBlack(x - 2, y - 1) || isEmpty(x - 2, y - 1)) setCheck(x - 2, y - 1);
									if (isWhite(x - 2, y + 1) || isBlack(x - 2, y + 1) || isEmpty(x - 2, y + 1)) setCheck(x - 2, y + 1);
									if (isWhite(x + 1, y - 2) || isBlack(x + 1, y - 2) || isEmpty(x + 1, y - 2)) setCheck(x + 1, y - 2);
									if (isWhite(x + 1, y + 2) || isBlack(x + 1, y + 2) || isEmpty(x + 1, y + 2)) setCheck(x + 1, y + 2);
									if (isWhite(x - 1, y - 2) || isBlack(x - 1, y - 2) || isEmpty(x - 1, y - 2)) setCheck(x - 1, y - 2);
									if (isWhite(x - 1, y + 2) || isBlack(x - 1, y + 2) || isEmpty(x - 1, y + 2)) setCheck(x - 1, y + 2);
								}
								if (color == PlayerColor.black) {
									if (isWhite(x + 2, y - 1) || isBlack(x + 2, y - 1) || isEmpty(x + 2, y - 1)) setCheck(x + 2, y - 1);
									if (isWhite(x + 2, y + 1) || isBlack(x + 2, y + 1) || isEmpty(x + 2, y + 1)) setCheck(x + 2, y + 1);
									if (isWhite(x - 2, y - 1) || isBlack(x - 2, y - 1) || isEmpty(x - 2, y - 1)) setCheck(x - 2, y - 1);
									if (isWhite(x - 2, y + 1) || isBlack(x - 2, y + 1) || isEmpty(x - 2, y + 1)) setCheck(x - 2, y + 1);
									if (isWhite(x + 1, y - 2) || isBlack(x + 1, y - 2) || isEmpty(x + 1, y - 2)) setCheck(x + 1, y - 2);
									if (isWhite(x + 1, y + 2) || isBlack(x + 1, y + 2) || isEmpty(x + 1, y + 2)) setCheck(x + 1, y + 2);
									if (isWhite(x - 1, y - 2) || isBlack(x - 1, y - 2) || isEmpty(x - 1, y - 2)) setCheck(x - 1, y - 2);
									if (isWhite(x - 1, y + 2) || isBlack(x - 1, y + 2) || isEmpty(x - 1, y + 2)) setCheck(x - 1, y + 2);
								}
							}
							break;
							case PieceType.bishop: {
								if (color == PlayerColor.white) {
									for (int i = 1; i < 8; i++) {
										if (isEmpty(x + i, y + i)) setCheck(x + i, y + i);
										else if (isBlack(x + i, y + i)&&getIcon(x + i, y + i).type==PieceType.king) {setCheck(x + i, y + i);}
										else  {setCheck(x + i, y + i); break;}

									}
									for (int i = 1; i < 8; i++) {
										if (isEmpty(x + i, y - i)) setCheck(x + i, y - i);
										else if (isBlack(x + i, y - i)&&getIcon(x + i, y - i).type==PieceType.king) {setCheck(x + i, y - i);}
										else  {setCheck(x + i, y - i); break;}
									}
									for (int i = 1; i < 8; i++) {
										if (isEmpty(x - i, y + i)) setCheck(x - i, y + i);
										else if (isBlack(x - i, y + i)&&getIcon(x - i, y + i).type==PieceType.king) {setCheck(x - i, y + i);}
										else  {setCheck(x - i, y + i); break;}
									}
									for (int i = 1; i < 8; i++) {
										if (isEmpty(x - i, y - i)) setCheck(x - i, y - i);
										else if (isBlack(x - i, y - i)&&getIcon(x - i, y - i).type==PieceType.king) {setCheck(x - i, y - i);}
										else  {setCheck(x - i, y - i); break;}
									}
								}
								if (color == PlayerColor.black) {
									for (int i = 1; i < 8; i++) {
										if (isEmpty(x + i, y + i)) setCheck(x + i, y + i);
										else if (isWhite(x + i, y + i)&&getIcon(x + i, y + i).type==PieceType.king) {setCheck(x + i, y + i);}
										else {setCheck(x + i, y + i); break;}
									}
									for (int i = 1; i < 8; i++) {
										if (isEmpty(x + i, y - i)) setCheck(x + i, y - i);
										else if (isWhite(x + i, y - i)&&getIcon(x + i, y - i).type==PieceType.king) {setCheck(x + i, y - i);}
										else {setCheck(x + i, y - i); break;}
									}
									for (int i = 1; i < 8; i++) {
										if (isEmpty(x - i, y + i)) setCheck(x - i, y + i);
										else if (isWhite(x - i, y + i)&&getIcon(x - i, y + i).type==PieceType.king) {setCheck(x - i, y + i);}
										else {setCheck(x - i, y + i); break;}
									}
									for (int i = 1; i < 8; i++) {
										if (isEmpty(x - i, y - i)) setCheck(x - i, y - i);
										else if (isWhite(x - i, y - i)&&getIcon(x - i, y - i).type==PieceType.king) {setCheck(x - i, y - i);}
										else {setCheck(x - i, y - i); break;}
									}
								}
							}
							break;
							case PieceType.rook: {
								if (color == PlayerColor.white) {
									for (int i = 1; i < 8; i++) {
										if (isEmpty(x + i, y)) setCheck(x + i, y);
										else if (isBlack(x + i, y)&&getIcon(x + i, y).type==PieceType.king) {setCheck(x + i, y);}
										else  {setCheck(x + i, y); break;}

									}
									for (int i = 1; i < 8; i++) {
										if (isEmpty(x, y - i)) setCheck(x, y - i);
										else if (isBlack(x, y - i)&&getIcon(x, y - i).type==PieceType.king) {setCheck(x, y - i);}
										else  {setCheck(x, y - i); break;}
									}
									for (int i = 1; i < 8; i++) {
										if (isEmpty(x, y + i)) setCheck(x, y + i);
										else if (isBlack(x, y + i)&&getIcon(x, y + i).type==PieceType.king) {setCheck(x, y + i);}
										else  {setCheck(x, y + i); break;}
									}
									for (int i = 1; i < 8; i++) {
										if (isEmpty(x - i, y)) setCheck(x - i, y);
										else if (isBlack(x - i, y)&&getIcon(x - i, y).type==PieceType.king) {setCheck(x - i, y);}
										else  {setCheck(x - i, y); break;}
									}
								}
								if (color == PlayerColor.black) {
									for (int i = 1; i < 8; i++) {
										if (isEmpty(x + i, y)) setCheck(x + i, y);
										else if (isWhite(x + i, y)&&getIcon(x + i, y).type==PieceType.king) {setCheck(x + i, y);}
										else  {setCheck(x + i, y); break;}

									}
									for (int i = 1; i < 8; i++) {
										if (isEmpty(x, y - i)) setCheck(x, y - i);
										else if (isWhite(x, y - i)&&getIcon(x, y - i).type==PieceType.king) {setCheck(x, y - i);}
										else  {setCheck(x, y - i); break;}
									}
									for (int i = 1; i < 8; i++) {
										if (isEmpty(x, y + i)) setCheck(x, y + i);
										else if (isWhite(x, y + i)&&getIcon(x, y + i).type==PieceType.king) {setCheck(x, y + i);}
										else  {setCheck(x, y + i); break;}
									}
									for (int i = 1; i < 8; i++) {
										if (isEmpty(x - i, y)) setCheck(x - i, y);
										else if (isWhite(x - i, y)&&getIcon(x - i, y).type==PieceType.king) {setCheck(x - i, y);}
										else  {setCheck(x - i, y); break;}
									}
								}
							}
							break;
							case PieceType.queen: {
								{
									if (color == PlayerColor.white) {
										for (int i = 1; i < 8; i++) {
											if (isEmpty(x + i, y + i)) setCheck(x + i, y + i);
											else if (isBlack(x + i, y + i)&&getIcon(x + i, y + i).type==PieceType.king) {setCheck(x + i, y + i);}
											else  {setCheck(x + i, y + i); break;}

										}
										for (int i = 1; i < 8; i++) {
											if (isEmpty(x + i, y - i)) setCheck(x + i, y - i);
											else if (isBlack(x + i, y - i)&&getIcon(x + i, y - i).type==PieceType.king) {setCheck(x + i, y - i);}
											else  {setCheck(x + i, y - i); break;}
										}
										for (int i = 1; i < 8; i++) {
											if (isEmpty(x - i, y + i)) setCheck(x - i, y + i);
											else if (isBlack(x - i, y + i)&&getIcon(x - i, y + i).type==PieceType.king) {setCheck(x - i, y + i);}
											else  {setCheck(x - i, y + i); break;}
										}
										for (int i = 1; i < 8; i++) {
											if (isEmpty(x - i, y - i)) setCheck(x - i, y - i);
											else if (isBlack(x - i, y - i)&&getIcon(x - i, y - i).type==PieceType.king) {setCheck(x - i, y - i);}
											else  {setCheck(x - i, y - i); break;}
										}
									}
									if (color == PlayerColor.black) {
										for (int i = 1; i < 8; i++) {
											if (isEmpty(x + i, y + i)) setCheck(x + i, y + i);
											else if (isWhite(x + i, y + i)&&getIcon(x + i, y + i).type==PieceType.king) {setCheck(x + i, y + i);}
											else {setCheck(x + i, y + i); break;}
										}
										for (int i = 1; i < 8; i++) {
											if (isEmpty(x + i, y - i)) setCheck(x + i, y - i);
											else if (isWhite(x + i, y - i)&&getIcon(x + i, y - i).type==PieceType.king) {setCheck(x + i, y - i);}
											else {setCheck(x + i, y - i); break;}
										}
										for (int i = 1; i < 8; i++) {
											if (isEmpty(x - i, y + i)) setCheck(x - i, y + i);
											else if (isWhite(x - i, y + i)&&getIcon(x - i, y + i).type==PieceType.king) {setCheck(x - i, y + i);}
											else {setCheck(x - i, y + i); break;}
										}
										for (int i = 1; i < 8; i++) {
											if (isEmpty(x - i, y - i)) setCheck(x - i, y - i);
											else if (isWhite(x - i, y - i)&&getIcon(x - i, y - i).type==PieceType.king) {setCheck(x - i, y - i);}
											else {setCheck(x - i, y - i); break;}
										}
									}
								}
								{
									if (color == PlayerColor.white) {
										for (int i = 1; i < 8; i++) {
											if (isEmpty(x + i, y)) setCheck(x + i, y);
											else if (isBlack(x + i, y)&&getIcon(x + i, y).type==PieceType.king) {setCheck(x + i, y);}
											else  {setCheck(x + i, y); break;}

										}
										for (int i = 1; i < 8; i++) {
											if (isEmpty(x, y - i)) setCheck(x, y - i);
											else if (isBlack(x, y - i)&&getIcon(x, y - i).type==PieceType.king) {setCheck(x, y - i);}
											else  {setCheck(x, y - i); break;}
										}
										for (int i = 1; i < 8; i++) {
											if (isEmpty(x, y + i)) setCheck(x, y + i);
											else if (isBlack(x, y + i)&&getIcon(x, y + i).type==PieceType.king) {setCheck(x, y + i);}
											else  {setCheck(x, y + i); break;}
										}
										for (int i = 1; i < 8; i++) {
											if (isEmpty(x - i, y)) setCheck(x - i, y);
											else if (isBlack(x - i, y)&&getIcon(x - i, y).type==PieceType.king) {setCheck(x - i, y);}
											else  {setCheck(x - i, y); break;}
										}
									}
									if (color == PlayerColor.black) {
										for (int i = 1; i < 8; i++) {
											if (isEmpty(x + i, y)) setCheck(x + i, y);
											else if (isWhite(x + i, y)&&getIcon(x + i, y).type==PieceType.king) {setCheck(x + i, y);}
											else  {setCheck(x + i, y); break;}

										}
										for (int i = 1; i < 8; i++) {
											if (isEmpty(x, y - i)) setCheck(x, y - i);
											else if (isWhite(x, y - i)&&getIcon(x, y - i).type==PieceType.king) {setCheck(x, y - i);}
											else  {setCheck(x, y - i); break;}
										}
										for (int i = 1; i < 8; i++) {
											if (isEmpty(x, y + i)) setCheck(x, y + i);
											else if (isWhite(x, y + i)&&getIcon(x, y + i).type==PieceType.king) {setCheck(x, y + i);}
											else  {setCheck(x, y + i); break;}
										}
										for (int i = 1; i < 8; i++) {
											if (isEmpty(x - i, y)) setCheck(x - i, y);
											else if (isWhite(x - i, y)&&getIcon(x - i, y).type==PieceType.king) {setCheck(x - i, y);}
											else  {setCheck(x - i, y); break;}
										}
									}
								}
							} break;
							case PieceType.king:{
								if (color == PlayerColor.white) {
									int i = 1;
									if (isWhite(x + i, y) || isBlack(x + i, y) || isEmpty(x + i, y)) setCheck(x + i, y);
									if (isWhite(x, y - i) || isBlack(x, y - i) || isEmpty(x, y - i)) setCheck(x, y - i);
									if (isWhite(x, y + i) || isBlack(x, y + i) || isEmpty(x, y + i)) setCheck(x, y + i);
									if (isWhite(x - i, y) || isBlack(x - i, y) || isEmpty(x - i, y)) setCheck(x - i, y);
									if (isWhite(x + i, y + i) || isBlack(x + i, y + i) || isEmpty(x + i, y + i)) setCheck(x + i, y + i);
									if (isWhite(x + i, y - i) || isBlack(x + i, y - i) || isEmpty(x + i, y - i)) setCheck(x + i, y - i);
									if (isWhite(x - i, y + i) || isBlack(x - i, y + i) || isEmpty(x - i, y + i)) setCheck(x - i, y + i);
									if (isWhite(x - i, y - i) || isBlack(x - i, y - i) || isEmpty(x - i, y - i)) setCheck(x - i, y - i);
								}
								if (color == PlayerColor.black){
									int i = 1;
									if (isWhite(x + i, y) || isBlack(x + i, y) || isEmpty(x + i, y)) setCheck(x + i, y);
									if (isWhite(x, y - i) || isBlack(x, y - i) || isEmpty(x, y - i)) setCheck(x, y - i);
									if (isWhite(x, y + i) || isBlack(x, y + i) || isEmpty(x, y + i)) setCheck(x, y + i);
									if (isWhite(x - i, y) || isBlack(x - i, y) || isEmpty(x - i, y)) setCheck(x - i, y);
									if (isWhite(x + i, y + i) || isBlack(x + i, y + i) || isEmpty(x + i, y + i)) setCheck(x + i, y + i);
									if (isWhite(x + i, y - i) || isBlack(x + i, y - i) || isEmpty(x + i, y - i)) setCheck(x + i, y - i);
									if (isWhite(x - i, y + i) || isBlack(x - i, y + i) || isEmpty(x - i, y + i)) setCheck(x - i, y + i);
									if (isWhite(x - i, y - i) || isBlack(x - i, y - i) || isEmpty(x - i, y - i)) setCheck(x - i, y - i);
								}
							}

                            case none:
                                break;
                        }
					}
				}
			}
		}

	}
	private void validMoveInCheck(Piece selPiece, int x, int y) {
		PieceType type = selPiece.type;
		PlayerColor color = selPiece.color;
		Piece temp;
		switch (type) {
			case PieceType.pawn: {
				if (color == PlayerColor.white) {
					if (x == 6)
						for (int i = 0; i < 2 && isEmpty(x - 1 - i, y); i++) {
							temp=getIcon(x-1-i,y);
							setIcon(x - 1 - i, y, selPiece);
							setIcon(x, y, new Piece());
							if(!isCheckState(color)) markPosition(x - 1 - i, y);
							setIcon(x - 1 - i, y, temp);
							setIcon(x, y, selPiece);
						}
					else {
						if (isEmpty(x - 1, y)){
							temp=getIcon(x-1,y);
							setIcon(x - 1, y, selPiece);
							setIcon(x, y, new Piece());
							if(!isCheckState(color)) markPosition(x - 1, y);
							setIcon(x - 1, y, temp);
							setIcon(x, y, selPiece);
						}
					}
					if (isBlack(x - 1, y - 1)) {
						temp=getIcon(x-1,y-1);
						setIcon(x - 1, y-1, selPiece);
						setIcon(x, y, new Piece());
						if(!isCheckState(color)) markPosition(x - 1, y-1);
						setIcon(x - 1, y-1, temp);
						setIcon(x, y, selPiece);
					}
					if (isBlack(x - 1, y + 1)) {
						temp=getIcon(x-1,y+1);
						setIcon(x - 1, y+1, selPiece);
						setIcon(x, y, new Piece());
						if(!isCheckState(color)) markPosition(x - 1, y+1);
						setIcon(x - 1, y+1, temp);
						setIcon(x, y, selPiece);
					}


				} else if (color == PlayerColor.black) {
					if (x == 1)
						for (int i = 0; i < 2 && isEmpty(x + 1 + i, y); i++) {
							temp=getIcon(x+1+i,y);
							setIcon(x + 1 + i, y, selPiece);
							setIcon(x, y, new Piece());
							if(!isCheckState(color)) markPosition(x + 1 + i, y);
							setIcon(x + 1 + i, y, temp);
							setIcon(x, y, selPiece);
						}
					else {
						if (isEmpty(x + 1, y)) {
							temp=getIcon(x+1,y);
							setIcon(x + 1, y, selPiece);
							setIcon(x, y, new Piece());
							if(!isCheckState(color)) markPosition(x + 1, y);
							setIcon(x + 1, y, temp);
							setIcon(x, y, selPiece);
						}
					}
					if (isWhite(x + 1, y - 1)) {
						temp=getIcon(x+1,y-1);
						setIcon(x + 1, y-1, selPiece);
						setIcon(x, y, new Piece());
						if(!isCheckState(color)) markPosition(x + 1, y-1);
						setIcon(x + 1, y-1, temp);
						setIcon(x, y, selPiece);
					}
					if (isWhite(x + 1, y + 1)) {
						temp=getIcon(x+1,y+1);
						setIcon(x + 1, y+1, selPiece);
						setIcon(x, y, new Piece());
						if(!isCheckState(color)) markPosition(x + 1, y+1);
						setIcon(x + 1, y+1, temp);
						setIcon(x, y, selPiece);
					}
				}
			}
			break;
			case PieceType.knight: {
				if (color == PlayerColor.white) {
					if (isBlack(x + 2, y - 1) || isEmpty(x + 2, y - 1)) validMoveInCheckTest(x,y,x+2,y-1,selPiece);
					if (isBlack(x + 2, y + 1) || isEmpty(x + 2, y + 1)) validMoveInCheckTest(x,y,x+2,y+1,selPiece);
					if (isBlack(x - 2, y - 1) || isEmpty(x - 2, y - 1)) validMoveInCheckTest(x,y,x-2,y-1,selPiece);
					if (isBlack(x - 2, y + 1) || isEmpty(x - 2, y + 1)) validMoveInCheckTest(x,y,x-2,y+1,selPiece);
					if (isBlack(x + 1, y - 2) || isEmpty(x + 1, y - 2)) validMoveInCheckTest(x,y,x+1,y-2,selPiece);
					if (isBlack(x + 1, y + 2) || isEmpty(x + 1, y + 2)) validMoveInCheckTest(x,y,x+1,y+2,selPiece);
					if (isBlack(x - 1, y - 2) || isEmpty(x - 1, y - 2)) validMoveInCheckTest(x,y,x-1,y-2,selPiece);
					if (isBlack(x - 1, y + 2) || isEmpty(x - 1, y + 2)) validMoveInCheckTest(x,y,x-1,y+2,selPiece);
				}
				if (color == PlayerColor.black) {
					if (isWhite(x + 2, y - 1) || isEmpty(x + 2, y - 1)) validMoveInCheckTest(x,y,x+2,y-1,selPiece);
					if (isWhite(x + 2, y + 1) || isEmpty(x + 2, y + 1)) validMoveInCheckTest(x,y,x+2,y+1,selPiece);
					if (isWhite(x - 2, y - 1) || isEmpty(x - 2, y - 1)) validMoveInCheckTest(x,y,x-2,y-1,selPiece);
					if (isWhite(x - 2, y + 1) || isEmpty(x - 2, y + 1)) validMoveInCheckTest(x,y,x-2,y+1,selPiece);
					if (isWhite(x + 1, y - 2) || isEmpty(x + 1, y - 2)) validMoveInCheckTest(x,y,x+1,y-2,selPiece);
					if (isWhite(x + 1, y + 2) || isEmpty(x + 1, y + 2)) validMoveInCheckTest(x,y,x+1,y+2,selPiece);
					if (isWhite(x - 1, y - 2) || isEmpty(x - 1, y - 2)) validMoveInCheckTest(x,y,x-1,y-2,selPiece);
					if (isWhite(x - 1, y + 2) || isEmpty(x - 1, y + 2)) validMoveInCheckTest(x,y,x-1,y+2,selPiece);
				}
			}
			break;
			case PieceType.bishop: {
				if (color == PlayerColor.white) {
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x + i, y + i)) validMoveInCheckTest(x,y,x+i,y+i,selPiece);
						else if (isBlack(x + i, y + i)) {validMoveInCheckTest(x,y,x+i,y+i,selPiece); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x + i, y - i)) validMoveInCheckTest(x,y,x+i,y-i,selPiece);
						else if (isBlack(x + i, y - i)) {validMoveInCheckTest(x,y,x+i,y-i,selPiece); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x - i, y + i)) validMoveInCheckTest(x,y,x-i,y+i,selPiece);
						else if (isBlack(x - i, y + i)) {validMoveInCheckTest(x,y,x-i,y+i,selPiece); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x - i, y - i)) validMoveInCheckTest(x,y,x-i,y-i,selPiece);
						else if (isBlack(x - i, y - i)) {validMoveInCheckTest(x,y,x-i,y-i,selPiece); break;}
						else break;
					}
				}
				if (color == PlayerColor.black) {
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x + i, y + i)) validMoveInCheckTest(x,y,x+i,y+i,selPiece);
						else if (isWhite(x + i, y + i)) {validMoveInCheckTest(x,y,x+i,y+i,selPiece); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x + i, y - i)) validMoveInCheckTest(x,y,x+i,y-i,selPiece);
						else if (isWhite(x + i, y - i)) {validMoveInCheckTest(x,y,x+i,y-i,selPiece); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x - i, y + i)) validMoveInCheckTest(x,y,x-i,y+i,selPiece);
						else if (isWhite(x - i, y + i)) {validMoveInCheckTest(x,y,x-i,y+i,selPiece); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x - i, y - i)) validMoveInCheckTest(x,y,x-i,y-i,selPiece);
						else if (isWhite(x - i, y - i)) {validMoveInCheckTest(x,y,x-i,y-i,selPiece); break;}
						else break;
					}
				}
			}
			break;
			case PieceType.rook: {
				if (color == PlayerColor.white) {
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x + i, y)) validMoveInCheckTest(x,y,x+i,y,selPiece);
						else if (isBlack(x + i, y)) {validMoveInCheckTest(x,y,x+i,y,selPiece); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x, y - i)) validMoveInCheckTest(x,y,x,y-i,selPiece);
						else if (isBlack(x, y - i)) {validMoveInCheckTest(x,y,x,y-i,selPiece); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x , y + i)) validMoveInCheckTest(x,y,x,y+i,selPiece);
						else if (isBlack(x, y + i)) {validMoveInCheckTest(x,y,x,y+i,selPiece); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x - i, y)) validMoveInCheckTest(x,y,x-i,y,selPiece);
						else if (isBlack(x - i, y)) {validMoveInCheckTest(x,y,x-i,y,selPiece); break;}
						else break;
					}
				}
				if (color == PlayerColor.black) {
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x + i, y)) validMoveInCheckTest(x,y,x+i,y,selPiece);
						else if (isWhite(x + i, y)) {validMoveInCheckTest(x,y,x+i,y,selPiece); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x, y - i)) validMoveInCheckTest(x,y,x,y-i,selPiece);
						else if (isWhite(x, y - i)) {validMoveInCheckTest(x,y,x,y-i,selPiece); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x , y + i)) validMoveInCheckTest(x,y,x,y+i,selPiece);
						else if (isWhite(x, y + i)) {validMoveInCheckTest(x,y,x,y+i,selPiece); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x - i, y)) validMoveInCheckTest(x,y,x-i,y,selPiece);
						else if (isWhite(x - i, y)) {validMoveInCheckTest(x,y,x-i,y,selPiece); break;}
						else break;
					}
				}
			}
			break;
			case PieceType.queen:{

				if (color == PlayerColor.white) {
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x + i, y + i)) validMoveInCheckTest(x,y,x+i,y+i,selPiece);
						else if (isBlack(x + i, y + i)) {validMoveInCheckTest(x,y,x+i,y+i,selPiece); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x + i, y - i)) validMoveInCheckTest(x,y,x+i,y-i,selPiece);
						else if (isBlack(x + i, y - i)) {validMoveInCheckTest(x,y,x+i,y-i,selPiece); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x - i, y + i)) validMoveInCheckTest(x,y,x-i,y+i,selPiece);
						else if (isBlack(x - i, y + i)) {validMoveInCheckTest(x,y,x-i,y+i,selPiece); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x - i, y - i)) validMoveInCheckTest(x,y,x-i,y-i,selPiece);
						else if (isBlack(x - i, y - i)) {validMoveInCheckTest(x,y,x-i,y-i,selPiece); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x + i, y)) validMoveInCheckTest(x,y,x+i,y,selPiece);
						else if (isBlack(x + i, y)) {validMoveInCheckTest(x,y,x+i,y,selPiece); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x, y - i)) validMoveInCheckTest(x,y,x,y-i,selPiece);
						else if (isBlack(x, y - i)) {validMoveInCheckTest(x,y,x,y-i,selPiece); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x , y + i)) validMoveInCheckTest(x,y,x,y+i,selPiece);
						else if (isBlack(x, y + i)) {validMoveInCheckTest(x,y,x,y+i,selPiece); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x - i, y)) validMoveInCheckTest(x,y,x-i,y,selPiece);
						else if (isBlack(x - i, y)) {validMoveInCheckTest(x,y,x-i,y,selPiece); break;}
						else break;
					}
				}
				if (color == PlayerColor.black) {
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x + i, y + i)) validMoveInCheckTest(x,y,x+i,y+i,selPiece);
						else if (isWhite(x + i, y + i)) {validMoveInCheckTest(x,y,x+i,y+i,selPiece); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x + i, y - i)) validMoveInCheckTest(x,y,x+i,y-i,selPiece);
						else if (isWhite(x + i, y - i)) {validMoveInCheckTest(x,y,x+i,y-i,selPiece); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x - i, y + i)) validMoveInCheckTest(x,y,x-i,y+i,selPiece);
						else if (isWhite(x - i, y + i)) {validMoveInCheckTest(x,y,x-i,y+i,selPiece); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x - i, y - i)) validMoveInCheckTest(x,y,x-i,y-i,selPiece);
						else if (isWhite(x - i, y - i)) {validMoveInCheckTest(x,y,x-i,y-i,selPiece); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x + i, y)) validMoveInCheckTest(x,y,x+i,y,selPiece);
						else if (isWhite(x + i, y)) {validMoveInCheckTest(x,y,x+i,y,selPiece); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x, y - i)) validMoveInCheckTest(x,y,x,y-i,selPiece);
						else if (isWhite(x, y - i)) {validMoveInCheckTest(x,y,x,y-i,selPiece); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x , y + i)) validMoveInCheckTest(x,y,x,y+i,selPiece);
						else if (isWhite(x, y + i)) {validMoveInCheckTest(x,y,x,y+i,selPiece); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x - i, y)) validMoveInCheckTest(x,y,x-i,y,selPiece);
						else if (isWhite(x - i, y)) {validMoveInCheckTest(x,y,x-i,y,selPiece); break;}
						else break;
					}
				}
			}
			break;
			case PieceType.king:{
				if (color == PlayerColor.white) {
					int i = 1;
					if (!getSpecial(x+1,y).check&&(isBlack(x + i, y) || isEmpty(x + i, y))) markPosition(x + i, y);
					if (!getSpecial(x,y-1).check&&(isBlack(x, y - i) || isEmpty(x, y - i))) markPosition(x, y - i);
					if (!getSpecial(x,y+1).check&&(isBlack(x, y + i) || isEmpty(x, y + i))) markPosition(x, y + i);
					if (!getSpecial(x-1,y).check&&(isBlack(x - i, y) || isEmpty(x - i, y))) markPosition(x - i, y);
					if (!getSpecial(x+1,y+1).check&&(isBlack(x + i, y + i) || isEmpty(x + i, y + i))) markPosition(x + i, y + i);
					if (!getSpecial(x+1,y-1).check&&(isBlack(x + i, y - i) || isEmpty(x + i, y - i))) markPosition(x + i, y - i);
					if (!getSpecial(x-1,y+1).check&&(isBlack(x - i, y + i) || isEmpty(x - i, y + i))) markPosition(x - i, y + i);
					if (!getSpecial(x-1,y-1).check&&(isBlack(x - i, y - i) || isEmpty(x - i, y - i))) markPosition(x - i, y - i);
				}
				if (color == PlayerColor.black){
					int i = 1;
					if (!getSpecial(x+1,y).check&&(isWhite(x + i, y) || isEmpty(x + i, y))) markPosition(x + i, y);
					if (!getSpecial(x,y-1).check&&(isWhite(x, y - i) || isEmpty(x, y - i))) markPosition(x, y - i);
					if (!getSpecial(x,y+1).check&&(isWhite(x, y + i) || isEmpty(x, y + i))) markPosition(x, y + i);
					if (!getSpecial(x-1,y).check&&(isWhite(x - i, y) || isEmpty(x - i, y))) markPosition(x - i, y);
					if (!getSpecial(x+1,y+1).check&&(isWhite(x + i, y + i) || isEmpty(x + i, y + i))) markPosition(x + i, y + i);
					if (!getSpecial(x+1,y-1).check&&(isWhite(x + i, y - i) || isEmpty(x + i, y - i))) markPosition(x + i, y - i);
					if (!getSpecial(x-1,y+1).check&&(isWhite(x - i, y + i) || isEmpty(x - i, y + i))) markPosition(x - i, y + i);
					if (!getSpecial(x-1,y-1).check&&(isWhite(x - i, y - i) || isEmpty(x - i, y - i))) markPosition(x - i, y - i);

				}
			}

		}
	}
	private void validMove(Piece selPiece, int x, int y) {
		PieceType type = selPiece.type;
		PlayerColor color = selPiece.color;
		switch (type) {
			case PieceType.pawn: {
				if (color == PlayerColor.white) {
					if (x == 6)
						for (int i = 0; i < 2 && isEmpty(x - 1 - i, y); i++) {
							markPosition(x - 1 - i, y);
						}
					else {
						if (isEmpty(x - 1, y))
							markPosition(x - 1, y);
					}
					if (isBlack(x - 1, y - 1)) markPosition(x - 1, y - 1);
					if (isBlack(x - 1, y + 1)) markPosition(x - 1, y + 1);


				} else if (color == PlayerColor.black) {
					if (x == 1)
						for (int i = 0; i < 2 && isEmpty(x + 1 + i, y); i++) {
							markPosition(x + 1 + i, y);
						}
					else {
						if (isEmpty(x + 1, y))
							markPosition(x + 1, y);
					}
					if (isWhite(x + 1, y - 1)) markPosition(x + 1, y - 1);
					if (isWhite(x + 1, y + 1)) markPosition(x + 1, y + 1);
				}
			}
			break;
			case PieceType.knight: {
				if (color == PlayerColor.white) {
					if (isBlack(x + 2, y - 1) || isEmpty(x + 2, y - 1)) markPosition(x + 2, y - 1);
					if (isBlack(x + 2, y + 1) || isEmpty(x + 2, y + 1)) markPosition(x + 2, y + 1);
					if (isBlack(x - 2, y - 1) || isEmpty(x - 2, y - 1)) markPosition(x - 2, y - 1);
					if (isBlack(x - 2, y + 1) || isEmpty(x - 2, y + 1)) markPosition(x - 2, y + 1);
					if (isBlack(x + 1, y - 2) || isEmpty(x + 1, y - 2)) markPosition(x + 1, y - 2);
					if (isBlack(x + 1, y + 2) || isEmpty(x + 1, y + 2)) markPosition(x + 1, y + 2);
					if (isBlack(x - 1, y - 2) || isEmpty(x - 1, y - 2)) markPosition(x - 1, y - 2);
					if (isBlack(x - 1, y + 2) || isEmpty(x - 1, y + 2)) markPosition(x - 1, y + 2);
				}
				if (color == PlayerColor.black) {
					if (isWhite(x + 2, y - 1) || isEmpty(x + 2, y - 1)) markPosition(x + 2, y - 1);
					if (isWhite(x + 2, y + 1) || isEmpty(x + 2, y + 1)) markPosition(x + 2, y + 1);
					if (isWhite(x - 2, y - 1) || isEmpty(x - 2, y - 1)) markPosition(x - 2, y - 1);
					if (isWhite(x - 2, y + 1) || isEmpty(x - 2, y + 1)) markPosition(x - 2, y + 1);
					if (isWhite(x + 1, y - 2) || isEmpty(x + 1, y - 2)) markPosition(x + 1, y - 2);
					if (isWhite(x + 1, y + 2) || isEmpty(x + 1, y + 2)) markPosition(x + 1, y + 2);
					if (isWhite(x - 1, y - 2) || isEmpty(x - 1, y - 2)) markPosition(x - 1, y - 2);
					if (isWhite(x - 1, y + 2) || isEmpty(x - 1, y + 2)) markPosition(x - 1, y + 2);
				}
			}
			break;
			case PieceType.bishop: {
				if (color == PlayerColor.white) {
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x + i, y + i)) markPosition(x + i, y + i);
						else if (isBlack(x + i, y + i)) {markPosition(x + i, y + i); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x + i, y - i)) markPosition(x + i, y - i);
						else if (isBlack(x + i, y - i)) {markPosition(x + i, y - i); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x - i, y + i)) markPosition(x - i, y + i);
						else if (isBlack(x - i, y + i)) {markPosition(x - i, y + i); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x - i, y - i)) markPosition(x - i, y - i);
						else if (isBlack(x - i, y - i)) {markPosition(x - i, y - i); break;}
						else break;
					}
				}
				if (color == PlayerColor.black) {
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x + i, y + i)) markPosition(x + i, y + i);
						else if (isWhite(x + i, y + i)) {markPosition(x + i, y + i); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x + i, y - i)) markPosition(x + i, y - i);
						else if (isWhite(x + i, y - i)) {markPosition(x + i, y - i); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x - i, y + i)) markPosition(x - i, y + i);
						else if (isWhite(x - i, y + i)) {markPosition(x - i, y + i); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x - i, y - i)) markPosition(x - i, y - i);
						else if (isWhite(x - i, y - i)) {markPosition(x - i, y - i); break;}
						else break;
					}
				}
			}
			break;
			case PieceType.rook: {
				if (color == PlayerColor.white) {
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x + i, y)) markPosition(x + i, y);
						else if (isBlack(x + i, y)) {markPosition(x + i, y); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x, y - i)) markPosition(x, y - i);
						else if (isBlack(x, y - i)) {markPosition(x, y - i); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x , y + i)) markPosition(x, y + i);
						else if (isBlack(x, y + i)) {markPosition(x, y + i); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x - i, y)) markPosition(x - i, y);
						else if (isBlack(x - i, y)) {markPosition(x - i, y); break;}
						else break;
					}
				}
				if (color == PlayerColor.black) {
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x + i, y)) markPosition(x + i, y);
						else if (isWhite(x + i, y)) {markPosition(x + i, y); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x, y - i)) markPosition(x, y - i);
						else if (isWhite(x, y - i)) {markPosition(x, y - i); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x , y + i)) markPosition(x, y + i);
						else if (isWhite(x, y + i)) {markPosition(x, y + i); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x - i, y)) markPosition(x - i, y);
						else if (isWhite(x - i, y)) {markPosition(x - i, y); break;}
						else break;
					}
				}
			} break;
			case PieceType.queen: {
				if (color == PlayerColor.white) {
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x + i, y + i)) markPosition(x + i, y + i);
						else if (isBlack(x + i, y + i)) {markPosition(x + i, y + i); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x + i, y - i)) markPosition(x + i, y - i);
						else if (isBlack(x + i, y - i)) {markPosition(x + i, y - i); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x - i, y + i)) markPosition(x - i, y + i);
						else if (isBlack(x - i, y + i)) {markPosition(x - i, y + i); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x - i, y - i)) markPosition(x - i, y - i);
						else if (isBlack(x - i, y - i)) {markPosition(x - i, y - i); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x + i, y)) markPosition(x + i, y);
						else if (isBlack(x + i, y)) {markPosition(x + i, y); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x, y - i)) markPosition(x, y - i);
						else if (isBlack(x, y - i)) {markPosition(x, y - i); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x , y + i)) markPosition(x, y + i);
						else if (isBlack(x, y + i)) {markPosition(x, y + i); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x - i, y)) markPosition(x - i, y);
						else if (isBlack(x - i, y)) {markPosition(x - i, y); break;}
						else break;
					}
				}
				if (color == PlayerColor.black) {
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x + i, y + i)) markPosition(x + i, y + i);
						else if (isWhite(x + i, y + i)) {markPosition(x + i, y + i); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x + i, y - i)) markPosition(x + i, y - i);
						else if (isWhite(x + i, y - i)) {markPosition(x + i, y - i); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x - i, y + i)) markPosition(x - i, y + i);
						else if (isWhite(x - i, y + i)) {markPosition(x - i, y + i); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x - i, y - i)) markPosition(x - i, y - i);
						else if (isWhite(x - i, y - i)) {markPosition(x - i, y - i); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x + i, y)) markPosition(x + i, y);
						else if (isWhite(x + i, y)) {markPosition(x + i, y); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x, y - i)) markPosition(x, y - i);
						else if (isWhite(x, y - i)) {markPosition(x, y - i); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x , y + i)) markPosition(x, y + i);
						else if (isWhite(x, y + i)) {markPosition(x, y + i); break;}
						else break;
					}
					for (int i = 1; i < 8; i++) {
						if (isEmpty(x - i, y)) markPosition(x - i, y);
						else if (isWhite(x - i, y)) {markPosition(x - i, y); break;}
						else break;
					}
				}
			} break;
			case PieceType.king:{
				if (color == PlayerColor.white) {
					int i = 1;
						if (isBlack(x + i, y) || isEmpty(x + i, y)) markPosition(x + i, y);
						if (isBlack(x, y - i) || isEmpty(x, y - i)) markPosition(x, y - i);
						if (isBlack(x, y + i) || isEmpty(x, y + i)) markPosition(x, y + i);
						if (isBlack(x - i, y) || isEmpty(x - i, y)) markPosition(x - i, y);
						if (isBlack(x + i, y + i) || isEmpty(x + i, y + i)) markPosition(x + i, y + i);
						if (isBlack(x + i, y - i) || isEmpty(x + i, y - i)) markPosition(x + i, y - i);
						if (isBlack(x - i, y + i) || isEmpty(x - i, y + i)) markPosition(x - i, y + i);
						if (isBlack(x - i, y - i) || isEmpty(x - i, y - i)) markPosition(x - i, y - i);
				}
				if (color == PlayerColor.black){
					int i = 1;
					if (isWhite(x + i, y) || isEmpty(x + i, y)) markPosition(x + i, y);
					if (isWhite(x, y - i) || isEmpty(x, y - i)) markPosition(x, y - i);
					if (isWhite(x, y + i) || isEmpty(x, y + i)) markPosition(x, y + i);
					if (isWhite(x - i, y) || isEmpty(x - i, y)) markPosition(x - i, y);
					if (isWhite(x + i, y + i) || isEmpty(x + i, y + i)) markPosition(x + i, y + i);
					if (isWhite(x + i, y - i) || isEmpty(x + i, y - i)) markPosition(x + i, y - i);
					if (isWhite(x - i, y + i) || isEmpty(x - i, y + i)) markPosition(x - i, y + i);
					if (isWhite(x - i, y - i) || isEmpty(x - i, y - i)) markPosition(x - i, y - i);

				}
			}

		}
	}
	private PlayerColor turn;
	private MagicType boardStatus;

	class ButtonListener implements ActionListener{
		int x;
		int y;
		ButtonListener(int x, int y){
			this.x = x;
			this.y = y;
		}
		public void actionPerformed(ActionEvent e) {	// Only modify here
			// (x, y) is where the click event occured
			Piece selPiece = getIcon(x, y);
			Boolean isMarked = chessBoardSquares[y][x].getBackground()==Color.pink;


			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) unmarkPosition(i, j);
			}

			if(end) return;
			//move piece
			if(isMarked){
				//castling
				if(prevSelPiece.type==PieceType.king&&getSpecial(prevSelx,prevSely).castling&&selPiece.type==PieceType.rook&&getSpecial(x,y).castling){
					if(y==7){
						setIcon(x, y, new Piece());
						setIcon(prevSelx, prevSely, new Piece());
						setIcon(x, 6, new Piece(prevSelPiece.color,PieceType.king));
						setIcon(x, 5, new Piece(prevSelPiece.color,PieceType.rook));
					}
					if(y==0){
						setIcon(x, y, new Piece());
						setIcon(prevSelx, prevSely, new Piece());
						setIcon(x, 2, new Piece(prevSelPiece.color,PieceType.king));
						setIcon(x, 3, new Piece(prevSelPiece.color,PieceType.rook));
					}
					changeTurn();
					if(getSpecial(prevSelx,prevSely).castling) getSpecial(prevSelx,prevSely).setCastling(false);
					for(int i=3;i<5;i++){
						for(int j=0;j<8;j++) {
							getSpecial(i,j).setEnpassant(false);
						}
					}
				}
				//enpassant
				if(prevSelPiece.type==PieceType.pawn&&getSpecial(prevSelx,y).enpassant){
					setIcon(prevSelx, prevSely, new Piece());
					setIcon(prevSelx, y, new Piece());
					setIcon(x, y, prevSelPiece);
					for(int i=3;i<5;i++){
						for(int j=0;j<8;j++) {
							getSpecial(i,j).setEnpassant(false);
						}
					}
					changeTurn();
				}
				//promotion
				if (prevSelPiece.type == PieceType.pawn && (x == 7 || x == 0)) {
					if(x==7) setIcon(x, y, new Piece(PlayerColor.black,PieceType.queen));
					if(x==0) setIcon(x, y, new Piece(PlayerColor.white,PieceType.queen));
					setIcon(prevSelx, prevSely, new Piece());
					for(int i=3;i<5;i++){
						for(int j=0;j<8;j++) {
							getSpecial(i,j).setEnpassant(false);
						}
					}
					changeTurn();

				}
				//normal
				else {
					setIcon(x, y, prevSelPiece);
					setIcon(prevSelx, prevSely, new Piece());
					changeTurn();
					if (getSpecial(prevSelx, prevSely).castling) getSpecial(prevSelx, prevSely).setCastling(false);
					for (int i = 3; i < 5; i++) {
						for (int j = 0; j < 8; j++) {
							getSpecial(i, j).setEnpassant(false);
						}
					}
					if (prevSelPiece.type == PieceType.pawn && ((x - prevSelx) * (x - prevSelx) == 4))
						getSpecial(x, y).setEnpassant(true);
				}
				check=false;


			} else if(turn!=selPiece.color){
			} else if(check){

				validMoveInCheck(selPiece,x,y);
				prevSelx=x;
				prevSely=y;
				prevSelPiece=selPiece;
			}
			//marking
			else {
				validMove(selPiece,x,y);
				//enpassant
				if(selPiece.type==PieceType.pawn){
					switch (selPiece.color){
						case PlayerColor.white:
							if(x==3&&isBlack(x,y+1)&&getSpecial(x,y+1).enpassant){
								markPosition(2,y+1);
							}
							if(x==3&&isBlack(x,y-1)&&getSpecial(x,y-1).enpassant){
								markPosition(2,y-1);
							}
							break;
						case PlayerColor.black:
							if(x==4&&isWhite(x,y+1)&&getSpecial(x,y+1).enpassant){
								markPosition(5,y+1);
							}
							if(x==4&&isWhite(x,y-1)&&getSpecial(x,y-1).enpassant){
								markPosition(5,y-1);
							}
							break;
					}
				}
				//castling
				if(selPiece.type==PieceType.king&&getSpecial(x,y).castling){
					if(getSpecial(x,0).castling){
						for(int i=1; i<4;i++) {
							if(getIcon(x,i).type==PieceType.none&&!isCheck(x,i)){
								markPosition(x,0);
							} else {
								unmarkPosition(x,0);
								break;
							}
						}
					}
					if(getSpecial(x,7).castling){
						for(int i=5; i<7;i++) {
							if(getIcon(x,i).type==PieceType.none&&!isCheck(x,i)){
								markPosition(x,7);
							} else {
								unmarkPosition(x,7);
								break;
							}
						}
					}
				}
				prevSelx=x;
				prevSely=y;
				prevSelPiece=selPiece;
			}

			//check인지 확인
			if(isCheckState(turn)) {
				check=true;
			}

			//checkmate인지 확인
			if(check){
				if(isCheckmateState(turn)){
					check=false;
					checkmate=true;
					end=true;
				}
			}


			if(turn == PlayerColor.white) {
				setStatus("WHITE's TURN");
				if(check) setStatus("WHITE's TURN/CHECK");
				if(checkmate) setStatus("WHITE's TURN/CHECKMATE");
			}
			if(turn == PlayerColor.black) {
				setStatus("BLACK's TURN");
				if(check) setStatus("BLACK's TURN/CHECK");
				if(checkmate) setStatus("BLACK's TURN/CHECKMATE");
			}
		}
	}
	
	void onInitiateBoard(){
		for(int i=0;i<8;i++){
			for(int j=0;j<8;j++) {
				SpecialState[j][i]=new Special();
				getSpecial(i,j).setCastling(false);
				getSpecial(i,j).setEnpassant(false);
			}
		}
		getSpecial(0,0).setCastling(true);
		getSpecial(0,7).setCastling(true);
		getSpecial(0,4).setCastling(true);
		getSpecial(7,0).setCastling(true);
		getSpecial(7,7).setCastling(true);
		getSpecial(7,4).setCastling(true);

		turn = PlayerColor.white;
		check=false;
		checkmate=false;
		end=false;
		setStatus("WHITE's TURN");

	}
}