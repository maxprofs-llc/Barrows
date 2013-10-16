package scripts.Barrows.methods;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.input.Keyboard;
import org.tribot.api.input.Mouse;
import org.tribot.api.interfaces.Positionable;
import org.tribot.api.types.generic.CustomRet_0P;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Camera;
import org.tribot.api2007.ChooseOption;
import org.tribot.api2007.Game;
import org.tribot.api2007.GroundItems;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.Objects;
import org.tribot.api2007.PathFinding;
import org.tribot.api2007.Player;
import org.tribot.api2007.Projection;
import org.tribot.api2007.Skills;
import org.tribot.api2007.Walking;
import org.tribot.api2007.types.RSGroundItem;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;

import scripts.Barrows.types.Brother;
import scripts.Barrows.types.Var;
import scripts.Barrows.util.PriceItem;
import scripts.Barrows.util.RSArea;

public class GeneralMethods {

	public static double getHPPercent() {
		return ((double) Skills.getCurrentLevel(Skills.SKILLS.HITPOINTS)
				/ (double) Skills.getActualLevel(Skills.SKILLS.HITPOINTS) * 100);
	}

	public boolean tunnelInterface() {
		return Interfaces.get(210, 0) != null;
	}

	public static Point getRandomPoint(Rectangle rec) {
		int randomX = getRandomInteger(rec.getMinX() + 10, rec.getMaxX() - 10);
		int randomY = getRandomInteger(rec.getMinY() + 10, rec.getMaxY() - 10);
		return new Point(randomX, randomY);
	}

	static int getRandomInteger(double min, double max) {
		return General.random((int) min, (int) max);
	}

	public static boolean contains(Object s, Object[] arr) {
		for (Object l : arr) {
			if (l == s)
				return true;
		}
		return false;
	}

	public static boolean contains(int s, int[] arr) {
		for (int l : arr) {
			if (l == s)
				return true;
		}
		return false;
	}

	// Fastest point clicking method (built for sudoku)
	public static void leftClick(final Point point) {
		DynamicClicking.clickPoint(new CustomRet_0P<Point>() {
			@Override
			public Point ret() {
				return point;
			}
		}, 1);
	}

	public static void rightClick(final Point point) {
		DynamicClicking.clickPoint(new CustomRet_0P<Point>() {
			@Override
			public Point ret() {
				return point;
			}
		}, 3);
	}

	// Finds average points in a Point[] use offset to make it random
	static Point getAverage(Point[] pointArray, int offset) {
		int averagex = 0;
		int averagey = 0;
		int totalx = 0;
		int totaly = 0;
		for (int i = 0; i < pointArray.length; i++) {
			totalx = totalx + pointArray[i].x;
			totaly = totaly + pointArray[i].y;
		}
		if (pointArray.length != 0) {
			averagex = totalx / pointArray.length;
			averagey = totaly / pointArray.length;
		}
		return new Point(averagex + General.random(-offset, offset), averagey
				+ General.random(-offset, offset));
	}

	static void leftClick(int x, int y) {
		leftClick(new Point(x, y));
	}

	public void assignNewBrother() {
		for (Brother.Brothers b : Brother.Brothers.values()) {
			if (!b.isKilled() && !b.isTunnel()) {
				if (Var.curBrother == null) {
					Var.curBrother = b;
				} else if (Var.curBrother.killOrder() > b.killOrder()) {
					Var.curBrother = b;
				}
			}
		}
	}

	public static void clickObject(RSObject o, String option,
			boolean minimapVisible) {
		clickObject(o, option, 0, minimapVisible);
	}

	public static boolean turnTo(final Positionable o) {
		if (o == null) {
			return false;
		}
		final int cAngle = Camera.getCameraRotation();
		final int angle = 180 + Camera.getTileAngle(o);
		final int dir = cAngle - angle;
		if (Math.abs(dir) <= 190 && Math.abs(dir) >= 180) {
			return false;
		}
		Camera.setCameraRotation(Camera.getCameraRotation()
				+ ((dir > 0 ^ Math.abs(dir) > 180) ? 10 : -10));
		return true;
	}

	private static boolean isObjectValid(RSObject o) {
		for (RSObject a : Objects.getAt((Positionable) o.getPosition())) {
			if (a.getID() == o.getID()) {
				return true;
			}
		}
		return false;
	}
	
	static void clickObject(RSObject o, String option, int fail, boolean minimap) {
		if (o == null || o.getModel() == null || Banking.isBankScreenOpen()
				|| Objects.find(50, o.getID()).length == 0 || !isObjectValid(o)
				)
			return;
		if (!o.isOnScreen() || fail > 2) {
			RSTile tile = o.getPosition();
			if (minimap) {
				Walking.control_click = true;
				Walking.walkTo(tile);
				General.sleep(250, 350);
				while (Player.isMoving() && !o.isOnScreen()) {
					turnTo(tile);
				}
				while (Player.isMoving())
					General.sleep(30, 50);
				if (!o.isOnScreen()) {
					clickObject(o, option, fail + 1, minimap);
				}
				fail = 0;
			} else {
				for (int i=0;i<5 && !o.isOnScreen();i++) {
					screenWalkTo(o);
				}
				if (!o.isOnScreen()) {
					clickObject(o, option, fail + 1, minimap);
				}
			}
		}
		if (fail > 2) {
			Camera.turnToTile(o);
		}
		if (fail > 4) {
			General.println("Failed to click Object: "
					+ o.getDefinition().getName() + "(" + o.getID() + ")");
			return;
		}
		while(Player.isMoving()) {
			General.sleep(15,30);
		}
		Var.debugObject = o;
		Var.centerPoint = getAverage(o.getModel().getAllVisiblePoints(),0);
		Point p = getAverage(o.getModel().getAllVisiblePoints(), 14);
		Mouse.move(p);
		for (int fSafe = 0; fSafe < 20 && !Game.getUptext().contains(option); fSafe++)
			General.sleep(10, 15);
		if (Game.getUptext().contains(option)
				|| Game.getUptext().contains("Use")) {

			Keyboard.pressKey((char) KeyEvent.VK_CONTROL);
			leftClick(p);
			Keyboard.releaseKey((char) KeyEvent.VK_CONTROL);
			Var.debugObject = null;
			Var.centerPoint = null;
			General.sleep(250, 350);
			while (Player.isMoving() && Player.getAnimation() == -1) {
				General.sleep(20, 30);
			}
		} else {
			rightClick(p);
			for (int fSafe = 0; fSafe < 20 && !ChooseOption.isOpen(); fSafe++)
				General.sleep(20, 25);
			if (ChooseOption.isOpen() && ChooseOption.isOptionValid(option)) {
				Keyboard.pressKey((char) KeyEvent.VK_CONTROL);
				ChooseOption.select(option);
				Keyboard.releaseKey((char) KeyEvent.VK_CONTROL);
				Var.debugObject = null;
				Var.centerPoint = null;
				General.sleep(250, 350);
				while (Player.isMoving() && Player.getAnimation() == -1) {
					General.sleep(20, 30);
				}
				return;
			} else if (ChooseOption.isOpen()) {
				ChooseOption.close();
				clickObject(o, option, fail + 1, minimap);
			} else {
				clickObject(o, option, fail + 1, minimap);
			}
		}
	}

	public static void screenWalkTo(Positionable p) {
		if (!Player.getPosition().equals(p)) {
			Positionable target = getClosestVisibleTile(p);
			Var.targetTile = (RSTile) target;
			Point i = Projection.tileToScreen(target, 0);
			for (int fSafe = 0; fSafe < 20 && !Game.getUptext().contains("Walk"); fSafe++)
				General.sleep(10, 15);
			if (Game.getUptext().contains("Walk")) {
				Keyboard.pressKey((char) KeyEvent.VK_CONTROL);
				leftClick(i);
				Keyboard.releaseKey((char) KeyEvent.VK_CONTROL);
				General.sleep(250,350);
			} else {
				rightClick(i);
				for (int fSafe = 0; fSafe < 20 && !ChooseOption.isOpen(); fSafe++)
					General.sleep(20, 25);
				if (ChooseOption.isOpen() && ChooseOption.isOptionValid("Walk")) {
					Keyboard.pressKey((char) KeyEvent.VK_CONTROL);
					ChooseOption.select("Walk");
					Keyboard.releaseKey((char) KeyEvent.VK_CONTROL);
					General.sleep(250,350);
				} else if (ChooseOption.isOpen()) {
					ChooseOption.close();
				}
			}
			while (Player.isMoving() 
					&& !Player.getPosition().equals(target)
					&& Player.getPosition().distanceTo(target) > 2
					&& Player.getPosition().distanceTo(p) > target.getPosition().distanceTo(p)) {
				General.sleep(25, 50);
			}
		}
	}
	
	private static Positionable getClosestVisibleTile(Positionable p) {
		RSTile closestTile = null;
		RSTile home = Player.getPosition();
		for (RSTile t : getViableTiles()) {
			if (closestTile==null) {
				if (!t.equals(home)
						&& t.distanceTo(p) < home.distanceTo(p)) {
					closestTile = t;
				}
			} else {
				if (!t.equals(home)
						&& t.distanceTo(p) < home.distanceTo(p)
						&& closestTile.distanceTo(p) > t.distanceTo(p)) {
					closestTile = t;
				}
			}
		}
		return closestTile;
	}

	private static ArrayList<RSTile> getViableTiles() {
		ArrayList<RSTile> tiles = new ArrayList<RSTile>();
		RSTile home = Player.getPosition();
		for (int x = home.getX()-20; x <= home.getX()+20;x++) {
			for (int y = home.getY()-20; y<=home.getY()+20;y++) {
				RSTile testTile = new RSTile(x,y);
				if (testTile.isOnScreen()
						&& PathFinding.canReach(testTile, false)
						&& PathFinding.isTileWalkable(testTile)
						&& !tiles.contains(testTile)) {
					tiles.add(testTile);
				}
			}
		}
		Var.viableTiles = tiles;
		return tiles;
	}

	public static RSArea getWithin(int distance, RSTile refe) {
		if (refe == null)
			return null;
		if (distance == 0)
			return null;
		RSTile southwest = new RSTile(refe.getX() - distance, refe.getY()
				- distance);
		RSTile northeast = new RSTile(refe.getX() + distance, refe.getY()
				+ distance);
		return new RSArea(southwest, northeast);
	}

	public static boolean click(RSNPC m, String option) {
		Mouse.setSpeed(500);
		if (m.isValid() && m.getModel()!=null) {
			if (m.getModel() != null)
				Mouse.move(GeneralMethods.getAverage(m.getModel()
						.getAllVisiblePoints(), 0));
			for (int fSafe = 0; fSafe < 20
					&& !Game.getUptext().contains(option + " " + m.getName()); fSafe++)
				General.sleep(5, 10);
			if (Game.getUptext().contains(option + " " + m.getName())) {
				Mouse.click(1);
				return true;
			} else {
				Mouse.click(GeneralMethods.getAverage(m.getModel()
						.getAllVisiblePoints(), 0), 3);
				General.sleep(80, 120);
				for (int fSafe = 0; fSafe < 20 && !ChooseOption.isOpen(); fSafe++)
					General.sleep(General.random(10, 15));
				if (ChooseOption.isOpen()) {
					if (ChooseOption.isOptionValid(option + " " + m.getName())) {
						return ChooseOption.select(option + " " + m.getName());
					} else {
						General.println("Misclicked");
						click(m, option);
					}
				}
			}
		}
		return false;
	}

	public static void setPrices() {
		String[] names = { "Guthans Warspear", "Guthans Chainskirt",
				"Guthans Helm", "Guthans Platebody", "Ahrims hood",
				"Ahrims staff", "Ahrims robetop", "Ahrims robeskirt",
				"Dharoks helm", "Dharoks greataxe", "Dharoks platebody",
				"Dharoks platelegs", "Karils coif", "Karils crossbow",
				"Karils leathertop", "Karils leatherskirt", "Bolt rack",
				"Torags helm", "Torags hammers", "Torags platebody",
				"Torags platelegs", "Veracs helm", "Veracs flail",
				"Veracs brassard", "Veracs plateskirt", "Blood rune",
				"Death rune", "Chaos rune", "Mind rune", "Dragon med helm",
				"Half of a key", "Loop half of a key" };

		int[] ids = { 4726, 4730, 4728, 4724, 4708, 4710, 4712, 4714, 4716,
				4718, 4720, 4722, 4732, 4734, 4736, 4738, 4740, 4745, 4747,
				4749, 4751, 4753, 4755, 4757, 4759, 565, 560, 562, 588, 1149,
				985, 987 };
		Var.lootIDs = ids;
		for (int i = 0; i < names.length; i++) {
			int price = 0;
			try {
				price = PriceItem.getPrice(names[i]);
			} catch (Exception e) {
			}
			if (price != 0) {
				Var.priceTable.put(ids[i], price);
				System.out.println("Id=" + ids[i] + "  |  Name=" + names[i]
						+ "  |  Price=" + price);
			} else {
				System.out.println("Failed to load prices for " + names[i]);
			}
		}
	}
	
	public static int getPrice(int id){
		return Var.priceTable.get(id);
	}

	private static boolean groundItemCheck(RSGroundItem g)
	{
		for (RSGroundItem a : GroundItems.getAt(g))
		{
			if (a.getID() == g.getID())
			{
				return true;
			}
		}
		return false;
	}
	
	public static void enableRun()
	{
		Keyboard.pressKey((char) KeyEvent.VK_CONTROL);
	}

	public static void disableRun()
	{
		Keyboard.releaseKey((char) KeyEvent.VK_CONTROL);
	}
	
	public static void leftClick(RSGroundItem n)
	{
		try
		{
			if (!groundItemCheck(n))
				return;
			Var.status = "Looting: " + n.getDefinition().getName();
			Mouse.setSpeed(General.random(110, 120));
			if (!n.isOnScreen())
			{
				Keyboard.pressKey((char) KeyEvent.VK_CONTROL);
				Walking.blindWalkTo(n);
				Keyboard.releaseKey((char) KeyEvent.VK_CONTROL);
				General.sleep(250, 350);
				while (Player.isMoving())
				{
					General.sleep(20, 30);
				}
			}
			if (n.getPosition().distanceTo(Player.getPosition()) > 8)
			{
				
				enableRun();
				Walking.blindWalkTo(n.getPosition());
				disableRun();
			}
			if (!groundItemCheck(n))
				return;
			Mouse.setSpeed(General.random(220, 300));
			enableRun();
			n.click("Take " + n.getDefinition().getName());
			disableRun();
			General.sleep(250, 350);
			while (Player.isMoving())
				General.sleep(40);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
