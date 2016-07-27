/* 
Programmer: Daniel Rojas
California State University, Northridge 
*/
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class FreeCellGame extends JPanel implements MouseListener, MouseMotionListener
{	
	CardImagePanel[] allCards = new CardImagePanel[52];
	int[] freeCells = new int[4];
	int[] columnHeights = new int[8];
	List<Stack<Integer>> foundations = new ArrayList<Stack<Integer>>();
	List<List<Integer>> columns = new ArrayList<List<Integer>>();
	ArrayList<Integer> cardGroups = new ArrayList<Integer>();
	
	private void initializeDataStructures()
	{
		int i = 8;
		while(i-- > 0)
		{
			columns.add(new ArrayList<Integer>());
		}
		i = 0;
		while(i < 4)
		{
			foundations.add(new Stack<Integer>());
			freeCells[i++] = -1;
		}
	}
	
	public boolean checkFreeCell(int index)
	{
		if(freeCells[index] == -1)
		{
			return true;
		}
		else return false;
	}
	
	public boolean checkFoundationInsert(int foundationIndex, Integer cardValue)
	{
		boolean foundationEmpty = foundations.get(foundationIndex).isEmpty();
		if(foundationEmpty && (cardValue.equals(0) || cardValue.equals(13) || 
							   cardValue.equals(26) || cardValue.equals(39)))
		{
			return true;
		}
		else if(!foundationEmpty && ((cardValue - foundations.get(foundationIndex).peek()) == 1))
		{
			return true;
		}
		return false;
	}
	
	public void fillFreeCell(int cellIndex, int cardIndex)
	{
		freeCells[cellIndex] = cardIndex;
	}
	
	public void fillFoundation(int foundationColumn, Integer cardNum) 
	{
		foundations.get(foundationColumn).push(cardNum);
	}
	
	public void removeOldspots(int cardIndex)
	{
		for(int i = 0; i < 4; i++)
		{
			if (freeCells[i] == cardIndex)
			{
				freeCells[i] = -1;
			}
			if(!foundations.get(i).isEmpty())
			{
				if(foundations.get(i).peek().equals(cardIndex))
				{
					foundations.get(i).pop();
				}
			}
		}
		for(List<Integer> curColumn : columns)
		{
			if(!curColumn.isEmpty())
			{
				for(int i = 0; i < curColumn.size(); i++)
				{
					if(curColumn.get(i) == cardIndex)
					{
						curColumn.remove(i);
						columnHeights[columns.indexOf(curColumn)] -= 40;
					}
				}
			}			
		}
	}
	
	public void updateCardStack(int columnIndex, int cardNumber)
	{
		columns.get(columnIndex).add(cardNumber);
		columnHeights[columnIndex]+= 40;
	}
	
	public void setNewDraggables()
	{
		for(int i = 0; i < 4; i++)
		{
			if (freeCells[i] != -1)
			{
				allCards[freeCells[i]].setDraggable(true);
			}
		}
		for(List<Integer> curColumn : columns)
		{
			if(!curColumn.isEmpty())
			{
				int lastElement = curColumn.size()-1;
				int index = curColumn.get(curColumn.size()-1);
				allCards[index].setDraggable(true);
				while(lastElement > 0)
				{
					allCards[curColumn.get(--lastElement)].setDraggable(false); 
				}
			}
		}
	}

	public FreeCellGame()
	{
		BufferedImage myPicture = null;
		try {
			myPicture = ImageIO.read(new 
               /*Replace this directory with the directory of your background file*/
					File("C:/Users/Daniel/Desktop/FreeCell/src/background.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		JLabel picLabel = new JLabel(new ImageIcon(myPicture));
		picLabel.setBounds(0, 0, 1208, 900);
		add(picLabel);
		
		this.initializeDataStructures();
		this.initiateColumnHeight();
		this.setLayout(null);
		this.addShapes();
		allCards = this.generateCards(allCards);
		this.placeCardsOnBoard(allCards);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.setBounds(0,0,1208,900);

		JFrame bottomLayer = new JFrame();
		bottomLayer.setSize(1208, 900);
		bottomLayer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		bottomLayer.setResizable(false);
		bottomLayer.add(this);
		bottomLayer.repaint();
		bottomLayer.revalidate();
		bottomLayer.setVisible(true);
	}
		
	public void addShapes() 
	{
		int xPos = 0;
		int yPos = 50;
		
		JLabel foundations = new JLabel("Foundations");
		foundations.setBounds(790, 50, 300, 50);
		foundations.setFont(new Font("Courier New", Font.BOLD, 30));
		foundations.setForeground(Color.white);
		this.add(foundations);
		this.setComponentZOrder(foundations, 0);
		
		JLabel freeCells = new JLabel("Free Cells");
		freeCells.setBounds(210, 200, 300, 50);
		freeCells.setFont(new Font("Courier New", Font.BOLD, 30));
		freeCells.setForeground(Color.white);
		this.add(freeCells);
		this.setComponentZOrder(freeCells, 0);
		
		for(int i = 0;  i < 8; i++)
		{
			JPanel rect = new JPanel();
			rect.setBorder(BorderFactory.createLineBorder(Color.white));
			rect.setBounds(xPos, yPos, 150, 150);
			rect.setOpaque(false);
			this.add(rect);
			this.setComponentZOrder(rect, 0);
			xPos+=150;
			if(i == 3)
			{
				yPos = 100;
			}
		}
	}
	public boolean checkUpperCard(Integer cardNumber)
	{
		for(int i = 0; i < 8; i++)
		{
			List<Integer>currentColumn = columns.get(i);
			for(int j = 0; j < columns.get(i).size(); j++)
			{
				if(cardNumber.equals(currentColumn.get(j)))
				{
					return checkDecreasingSequence(i,j);
				}
			}
		}
		return false;
	}

	private boolean checkDecreasingSequence(int columnIndex, int cardIndex) 
	{
		int size = columns.get(columnIndex).size(),
			indexDifference = size - cardIndex -1,
			currentCard = columns.get(columnIndex).get(cardIndex) % 13,
			lastCard = columns.get(columnIndex).get(size-1)% 13;
		boolean result = (currentCard - lastCard) == indexDifference;
		if(result)
		{
			for(int i = cardIndex; i < size; i++)
			{
				cardGroups.add(columns.get(columnIndex).get(i));
			}
		}
			return result;
	}

	public CardImagePanel[] generateCards(CardImagePanel[] allCards)
	{
     /*Replace this directory with the directory of your file*/
	  File folder = new File("C:/Users/Daniel/Desktop/FreeCell/src/images");
	  File[] listOfFiles = folder.listFiles();
	  allCards = new CardImagePanel[52];
	  for (File file : listOfFiles) {
		if (file.isFile()) {
		  CardImagePanel currentCard = new CardImagePanel(file);
		  currentCard.setPreferredSize(new Dimension(150, 150));
		  allCards[currentCard.getCardNum()] = currentCard;
		}
	  }
	  return allCards;
	}
	
	private void initiateColumnHeight()
	{
		for(int i = 0; i < 8; i++)
		{
			columnHeights[i] = 300;
		}
	}
	
	public int getColumnHeight(int columnIndex)
	{
		return columnHeights[columnIndex];
	}
	
	private ArrayList<Integer> createRandomArrayList()
	{
		ArrayList<Integer> randomUniqueValues = new ArrayList<Integer>();
		int count = 52;
		while(--count >= 0)
		{
			randomUniqueValues.add(count);
		}
		Collections.shuffle(randomUniqueValues);
		return randomUniqueValues;
	}
	
	public void placeCardsOnBoard(CardImagePanel[] allCards)
	{
		int x = 0;
		int columnCount = 0;
		int count = 0;
		
		for(Integer newRandomIndex : createRandomArrayList())
		{
			CardImagePanel currentCard = allCards[newRandomIndex];
			currentCard.setBounds(x, columnHeights[columnCount], 150, 150);
			currentCard.setPosition(x, columnHeights[columnCount]);
			currentCard.setOpaque(false);
			currentCard.addMouseListener(currentCard);
			currentCard.addMouseMotionListener(currentCard);
			
			if(count == 6 || count == 13 || count == 20 || count == 27 || count == 33 || count == 39 || count == 45 || count == 51)
			{
				currentCard.setDraggable(true);
				columns.get(columnCount).add(currentCard.getCardNum());
				this.add(currentCard);
				this.setComponentZOrder(currentCard, 0);
				x+=150;
				++columnCount;
			}
			else
			{
				columns.get(columnCount).add(currentCard.getCardNum());
				columnHeights[columnCount] += 40;
				this.add(currentCard);
				this.setComponentZOrder(currentCard, 0);
			}
			count++;
		}
	}
	
	public boolean checkColumnInsert(int colmnIndex, int cardNum)
	{
		if(columns.get(colmnIndex).isEmpty())
		{
			return true;
		}
		else
		{
			int lastElement = columns.get(colmnIndex).size() - 1,
			    topStackCardForColumn = columns.get(colmnIndex).get(lastElement);
			if(checkDifferentSuit(topStackCardForColumn, cardNum))
			{
				if((topStackCardForColumn%13) - (cardNum%13) == 1)
				{
					return true;
				}
			}
			return false;
		}
	}
	
	private boolean checkDifferentSuit(int topCard, int cardNum)
	{
		boolean topCardIsBlack = topCard <= 12 || topCard >= 39,
				newCardIsBlack = cardNum <= 12 || cardNum >= 39;
		return newCardIsBlack != topCardIsBlack;
	}
	
	public static void main(String[] args){
		@SuppressWarnings("unused")
		FreeCellGame newGame = new FreeCellGame();
	}
			
	public void mouseDragged(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
 }