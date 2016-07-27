import java.awt.Graphics;
import java.awt.event.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class CardImagePanel extends JPanel implements MouseListener, MouseMotionListener
{
	private boolean eventStarted = false,
	                allowDrag = false,
	                multipleDrag = false;
	private BufferedImage picture;	
	private int cardNum,
				currentXPosition = 0,
	            currentYPosition = 0;

	public CardImagePanel(File newImageFile)
	{
		this.setLayout(null);
		try {
			picture = ImageIO.read(newImageFile);
	
		} catch (IOException e){
			e.printStackTrace();
		}
		String tempString = newImageFile.getName();
		cardNum = Integer.parseInt(tempString.substring(0,tempString.lastIndexOf('.')));
	}
	
	public void paintComponent(Graphics g) 
	{
      super.paintComponent(g);
      g.drawImage(picture, 1, 0, 148, 150, this);
    }
	
	private void dropInFreeCells(int xPos)
	{
		int freeCellColumn = getColumn(xPos);
		if(((FreeCellGame) this.getParent()).checkFreeCell(freeCellColumn))
		{
			((FreeCellGame) this.getParent()).removeOldspots(this.getCardNum());
			((FreeCellGame) this.getParent()).setNewDraggables();
			((FreeCellGame) this.getParent()).fillFreeCell(freeCellColumn, this.cardNum);
			this.setBounds(freeCellColumn*150, 50, 150, 150);
			setPosition(freeCellColumn*150, 50);
		}
		else
		{
			this.setBounds(currentXPosition, currentYPosition, 150, 150);
		}
	}
	
	private void dropInFoundations(int xPos)
	{
		int foundationColumn = getColumn(xPos);
		if(((FreeCellGame) this.getParent()).checkFoundationInsert(foundationColumn-4, this.cardNum))
		{
			((FreeCellGame) this.getParent()).removeOldspots(this.getCardNum());
			((FreeCellGame) this.getParent()).setNewDraggables();
			((FreeCellGame) this.getParent()).fillFoundation(foundationColumn-4, this.cardNum);
			this.setBounds(foundationColumn*150, 100, 150, 150);
			setPosition(foundationColumn*150, 100);
		}
		else
		{
			this.setBounds(currentXPosition, currentYPosition, 150, 150);
		}
	}
	
	private void singleCardStackDrop(int xPos, int yPos)
	{
		int columnIndex = getColumn(xPos);
		int currentColumnHeight = ((FreeCellGame) this.getParent()).getColumnHeight(columnIndex);
		if(yPos > currentColumnHeight && yPos < (currentColumnHeight + 150) &&
				((FreeCellGame) this.getParent()).checkColumnInsert(columnIndex, this.getCardNum()))
		{
			((FreeCellGame) this.getParent()).removeOldspots(this.getCardNum());
			((FreeCellGame) this.getParent()).updateCardStack(columnIndex, this.getCardNum());
			int updatedHeight = ((FreeCellGame) this.getParent()).getColumnHeight(columnIndex);
			this.setBounds(columnIndex*150, updatedHeight, 150, 150);
			setPosition(columnIndex*150, updatedHeight);
			((FreeCellGame) this.getParent()).setNewDraggables();
		}
		else
		{
			this.setBounds(currentXPosition, currentYPosition, 150, 150);
		}
	}
	
	private void multipleCardStackDrop(int xPos, int yPos)
	{
		int columnIndex = getColumn(xPos),
			    currentColumnHeight = ((FreeCellGame) this.getParent()).getColumnHeight(columnIndex);
			ArrayList<Integer> cardsToMove = ((FreeCellGame) this.getParent()).cardGroups;
			if(yPos > currentColumnHeight && yPos < (currentColumnHeight + 150) &&
					((FreeCellGame) this.getParent()).checkColumnInsert(columnIndex, this.getCardNum()))
			{
				int updatedHeight;
				for(Integer card : cardsToMove)
				{
					((FreeCellGame) this.getParent()).removeOldspots(card);
					((FreeCellGame) this.getParent()).updateCardStack(columnIndex, card);
					updatedHeight = ((FreeCellGame) this.getParent()).getColumnHeight(columnIndex);
					((FreeCellGame) this.getParent()).allCards[card].setBounds(columnIndex*150, updatedHeight, 150, 150);
					((FreeCellGame) this.getParent()).allCards[card].setPosition(columnIndex*150, updatedHeight);
				}
				((FreeCellGame) this.getParent()).setNewDraggables();
				((FreeCellGame) this.getParent()).cardGroups.clear();
			}
			else
			{
				dropMultipleCards();
			}
	}
	
	public void checkDropLocation(int x, int y)
	{	
		x +=75;
		y +=75;
		if(x < 600 && y > 50 && y < 200 && !multipleDrag)
		{
			dropInFreeCells(x);
		}
		else if(x > 600 && y > 100 && y < 250 && !multipleDrag)
		{
			dropInFoundations(x);
		}
		else if(y > 300 && !multipleDrag)
		{ 
			singleCardStackDrop(x,y);
		}
		else if(y > 300)
		{
			multipleCardStackDrop(x,y);
		}
		else 
		{ 
			dropBackToOriginalSpot(multipleDrag);
		}
	}
	
	private void dropBackToOriginalSpot(boolean multiple)
	{
		if(!multiple)
		{
			this.setBounds(currentXPosition, currentYPosition, 150, 150);				
		}
		else
		{
			dropMultipleCards();
		}
	}
	
	private void dropMultipleCards()
	{
		int addCardYPos = currentYPosition;
		ArrayList<Integer> cardsToMove = ((FreeCellGame) this.getParent()).cardGroups;
		for(int i = 0; i < cardsToMove.size(); i++)
		{
			((FreeCellGame)this.getParent()).allCards[cardsToMove.get(i)]
					.setBounds(currentXPosition, addCardYPos, 150, 150);
			addCardYPos += 40;
		}
		((FreeCellGame) this.getParent()).cardGroups.clear();
	}
		
	public void mouseDragged(MouseEvent e) 
	{
		if(allowDrag) 
		{
			if(!eventStarted)
			{
				this.getParent().setComponentZOrder(this, 0);
				eventStarted = true;
			}
			this.setBounds(e.getX() + this.getX()-75, e.getY() + this.getY()-75, 150, 150); //move center of card to mouse
		}
		else if(multipleDrag)
		{
			ArrayList<Integer> cardsToMove = ((FreeCellGame) this.getParent()).cardGroups;
			int offset = 0;
			int xPosition = e.getX() + this.getX()-75,
				yPosition = e.getY() + this.getY()-75;
			
			for(Integer cards : cardsToMove)
			{
				((FreeCellGame)this.getParent()).allCards[cards].setBounds(xPosition, 
																		   yPosition + offset, 
																		   150, 150);
				this.getParent().setComponentZOrder(((FreeCellGame)this.getParent()).allCards[cards], 0);
				offset+=40;
			}
		}
		else if(((FreeCellGame) this.getParent()).checkUpperCard(this.getCardNum()))
		{
			multipleDrag = true;
		}
	}
	
	public void mouseReleased(MouseEvent e) 
	{
		if(allowDrag)
		{
			checkDropLocation(this.getX(), this.getY());
			eventStarted = false;
		}
		else if(multipleDrag)
		{
			checkDropLocation(this.getX(), this.getY());
			multipleDrag = false;
		}
	}
		
	public void mousePressed(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	public int getCardNum(){
		return cardNum;
	}
	public int getColumn(int x){
		return x/150;
	}
	public BufferedImage getPicture(){
		return picture;
	}
	public void setDraggable(boolean drag){
		allowDrag = drag;
	}
	public void setPosition(int x, int y){
		this.currentXPosition = x;
		this.currentYPosition = y;
	}
}