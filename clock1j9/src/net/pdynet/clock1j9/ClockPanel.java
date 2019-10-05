package net.pdynet.clock1j9;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

class ClockPanel extends JPanel implements Runnable {

	private static final long serialVersionUID = 1L;
	private static final int HORIZONTAL_SIZE = 500;
	private static final int VERTICAL_SIZE = 500;
	private static final Color GRAY_COLOR = new Color(160, 160, 160);
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d.M.yyyy");
	
	private Thread t = new Thread(this);
	private Font clockFont = null;
	private Font nameFont = null;
	private int lastSecOfDay = -1;

	public ClockPanel() {
		super(null, true);
		
		// Inicializace fontu.
		initFont();
		
		// Rozmery panelu.
		setMinimumSize(new Dimension(HORIZONTAL_SIZE, VERTICAL_SIZE));
		setMaximumSize(new Dimension(HORIZONTAL_SIZE, VERTICAL_SIZE));
		setPreferredSize(new Dimension(HORIZONTAL_SIZE, VERTICAL_SIZE));
		
		// Spusteni vlakna pro hlidani zmen casu a kresleni hodin.
		t.start();
	}
	
	public void run() {
		boolean loop = true;
		
		// Dokud nedojde k vyjimce nebo ukonceni aplikace.
		while (loop) {
			try {
				// Aktualni cas (hodiny konkretniho dne).
				LocalTime now = LocalTime.now();
				
				// Celkovy pocet sekund od pulnoci.
				int secOfDay = now.toSecondOfDay();
				
				// Porovnani s naposledy ulozenou hodnotou.
				// Pokud se zmenil pocet sekund od posledni ulozene hodnoty,
				// vyvola se udalost pro prekresleni hodin.
				if (secOfDay != lastSecOfDay) {
					lastSecOfDay = secOfDay;
					repaint();
				}
				
				// Uspani vlakna na 250ms.
				Thread.sleep(250);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
				loop = false;
			}
		}
	}

	/**
	 * Vykresli cifernik hodin.
	 * 
	 * @param g2
	 */
	protected void paintClockBackground(Graphics2D g2) {
		// Sirka, vyska a stred okna.
		int width = getWidth();
		int height = getHeight();
		int centerX = width / 2;
		int centerY = height / 2;
		
		// Polomery pro rozdeleni ciselniku a stredu cislic s hodinami.
		int radiusMax = Math.min(width, height) / 2 - 4;
		int radiusMin1 = Math.min(width, height) / 2 - 12;
		int radiusMin2 = Math.min(width, height) / 2 - 24;
		int radiusText = Math.min(width, height) / 2 - 50;
		
		int textWidth, textHeight;
		String str;
		TextLayout layout;
		Rectangle2D bounds;
		
		// Vykresleni podkladu s prechodem.
		RadialGradientPaint rgp = new RadialGradientPaint(
				new Point(getWidth() / 2, getHeight() / 2),
				width,
				new float[] {0.2f, 0.9f},
				new Color[] {new Color(106, 145, 66), Color.WHITE}
				);
		g2.setPaint(rgp);
		g2.fillOval(2, 2, width-4, height-4);
		
		// Nastaveni fontu pro cislice a inicializace tridy FontRenderContext,
		// ktera se pouzije pro vypocet rozmeru potrebnych na vykresleni textu.
		g2.setFont(clockFont);
		FontRenderContext frc = g2.getFontRenderContext();
        
		// 3. hodina = uhel 0°
		int hour = 3;
		
		// Barva car a textu.
		g2.setColor(Color.ORANGE);
		
		// Vykresleni ciferniku po krocich 6°.
		for (double angle = 0d; angle < 360d; angle += 6d) {
			// Vypocet souradnice pro caru dal od stredu.
			Point2D point2 = anglePoint(centerX, centerY, radiusMax, angle);
			Point2D point1;
			
			if (angle % 30 == 0) {
				// Pokud je uhel delitelny 30 beze zbytku, kreslime silnejsi caru a cislici.
				
				// Souradnice stredu cislice s hodinou.
				point1 = anglePoint(centerX, centerY, radiusText, angle);
				
				// Prevod cisla s hodinami na text, vypocet potrebnych rozmeru
				// na vykresleni textu pouzitym fontem a jeho samotne vykresleni.
				str = Integer.toString(hour);
				layout = new TextLayout(str, clockFont, frc);
				bounds = layout.getBounds();
				textWidth = (int) Math.round(bounds.getWidth());
				textHeight = (int) Math.round(bounds.getHeight());
				g2.drawString(str, (float)point1.getX() - textWidth / 2, (float)point1.getY() + textHeight / 2);
				
				// Zvetseni hodnoty pro dalsi hodinu, ktera se bude kreslit.
				// Pokud by cislo prekrocilo hodnotu 12, o 12 se snizi.
				if (++hour > 12)
					hour -= 12;
				
				// Vypocet souradnice silnejsi cary blize ke stredu a nastaveni sirky cary.
				g2.setStroke(new BasicStroke(4));
				point1 = anglePoint(centerX, centerY, radiusMin2, angle);
			} else {
				// Vypocet souradnice tenci cary blize ke stredu a nastaveni sirky cary.
				g2.setStroke(new BasicStroke(3));
				point1 = anglePoint(centerX, centerY, radiusMin1, angle);
			}
			
			// Vkresleni cary rozdelujici cifernik.
			Line2D line = new Line2D.Double(point1, point2);
			g2.draw(line);
		}
		
		// Vykresleni datumu.
		LocalDate today = LocalDate.now();
		str = DATE_FORMAT.format(today);
		layout = new TextLayout(str, clockFont, frc);
		bounds = layout.getBounds();
		textWidth = (int) Math.round(bounds.getWidth());
		textHeight = (int) Math.round(bounds.getHeight());
		g2.drawString(str, centerX - textWidth / 2, centerY - textHeight - 40);
		
		// Tady se kresli ta nejlepsi cast. :)
		str = "Richard Koudelka";
		g2.setFont(nameFont);
		layout = new TextLayout(str, nameFont, frc);
		bounds = layout.getBounds();
		textWidth = (int) Math.round(bounds.getWidth());
		textHeight = (int) Math.round(bounds.getHeight());
		g2.drawString(str, centerX - textWidth / 2, centerY + textHeight + 60);
		
		// Ohraniceni ciferniku silnejsi sedou kruznici a tenci cernou.
		g2.setColor(GRAY_COLOR);
		g2.setStroke(new BasicStroke(3));
		g2.drawOval(2, 2, width - 5, height - 5);
		
		g2.setColor(Color.BLACK);
		g2.setStroke(new BasicStroke(1));
		g2.drawOval(2, 2, width - 5, height - 5);
	}
	
	/**
	 * Vykresleni rucicek.
	 * 
	 * @param g2
	 */
	protected void paintClockHands(Graphics2D g2) {
		// Aktualni cas.
		LocalTime time = LocalTime.now();
		
		// Sirka, vyska a stred okna.
		int width = getWidth();
		int height = getHeight();
		int centerX = width / 2;
		int centerY = height / 2;
		Point2D center = new Point2D.Double(centerX, centerY);
		
		// Polomery rucicek.
		int radiusHour = Math.min(width, height) / 2 - 80;
		int radiusMinute = Math.min(width, height) / 2 - 40;
		int radiusSecond = Math.min(width, height) / 2 - 30;
		
		// 86400 sekund za den.
		// time.toSecondOfDay() / 120 = time.toSecondOfDay() / 86400 * 2 * 360 
		double hourAngle = time.toSecondOfDay() / 120d - 90d;
		Point2D point = anglePoint(centerX, centerY, radiusHour, hourAngle);
		g2.setColor(Color.BLACK);
		g2.setStroke(new BasicStroke(12, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
		Line2D line = new Line2D.Double(center, point);
		g2.draw(line);
		//System.out.println(hourAngle);
		
		// (time.getMinute() * 60 + time.getSecond()) / 10 = (time.getMinute() * 60 + time.getSecond()) / 3600 * 360
		double minuteAngle = (time.getMinute() * 60 + time.getSecond()) / 10d - 90d;
		point = anglePoint(centerX, centerY, radiusMinute, minuteAngle);
		g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
		line = new Line2D.Double(center, point);
		g2.draw(line);
		//System.out.println(minuteAngle);
		
		// time.getSecond() * 6 = time.getSecond() / 60 * 360
		double secondAngle = time.getSecond() * 6d - 90d;
		point = anglePoint(centerX, centerY, radiusSecond, secondAngle);
		g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
		g2.setColor(Color.RED);
		line = new Line2D.Double(center, point);
		g2.draw(line);
		//System.out.println(secondAngle);
		
		g2.setColor(Color.RED);
		g2.fillOval(centerX - 10, centerY - 10, 20, 20);
		g2.setColor(Color.ORANGE);
		g2.fillOval(centerX - 5, centerY - 5, 10, 10);
	}
	
	/**
	 * Vraci souradince bodu v zavislosti na stredu, uhlu a polomeru.
	 * 
	 * @param centerX X souradnice stredu.
	 * @param centerY Y souradnice stredu.
	 * @param radius Polomer.
	 * @param angle Uhel ve stupnich.
	 * @return
	 */
	private Point2D anglePoint(int centerX, int centerY, int radius, double angle) {
		// Uhel se prevadi ze stupnu na Radiany, protoze funkce sin a cos pracuji v Radianech.
		double x = centerX + (radius * Math.cos(Math.toRadians(angle)));
		double y = centerY + (radius * Math.sin(Math.toRadians(angle)));
		return new Point2D.Double(x, y);
		/*
		BigDecimal bdX = BigDecimal.valueOf(x);
		BigDecimal bdY = BigDecimal.valueOf(y);
		
		return new Point2D.Double(bdX.setScale(0, java.math.RoundingMode.HALF_UP).intValue(), bdY.setScale(0, java.math.RoundingMode.HALF_UP).intValue());
		//return new Point(bdX.setScale(0, java.math.RoundingMode.HALF_UP).intValue(), bdY.setScale(0, java.math.RoundingMode.HALF_UP).intValue());
		*/
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		g2.setBackground(Color.WHITE);
		g2.clearRect(0, 0, getWidth(), getHeight());
		
		paintClockBackground(g2);
		paintClockHands(g2);
	}
	
	/**
	 * Inicializuje pouzite fonty.
	 */
	private void initFont() {
		try {
			try (InputStream is = ClockPanel.class.getResourceAsStream("/Privus-Medium.otf")) {
				Font font = Font.createFont(Font.TRUETYPE_FONT, is);
				Map<TextAttribute, Object> attributes = new HashMap<>();
				attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR);
				attributes.put(TextAttribute.SIZE, 42);
				clockFont = font.deriveFont(attributes);
			}
			
			try (InputStream is = ClockPanel.class.getResourceAsStream("/Pallaraja.ttf")) {
				Font font = Font.createFont(Font.TRUETYPE_FONT, is);
				Map<TextAttribute, Object> attributes = new HashMap<>();
				attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR);
				attributes.put(TextAttribute.SIZE, 38);
				nameFont = font.deriveFont(attributes);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
