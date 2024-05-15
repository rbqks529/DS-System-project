import java.awt.*;

public class Shape {
	private String type;
	private Point startPoint;
	private Point endPoint;
	private Color lineColor;
	private Color fillColor;
	private int thickness;
	private boolean fill;
	private String text;

	public Shape(String type, int x1, int y1, int x2, int y2, Color lineColor, Color fillColor, int thickness, boolean fill, String text) {
		this.type = type;
		this.startPoint = new Point(x1, y1);
		this.endPoint = new Point(x2, y2);
		this.lineColor = lineColor;
		this.fillColor = fillColor;
		this.thickness = thickness;
		this.fill = fill;
		this.text = text;
	}

	// 정적 메소드 추가
	public static Shape createShapeFromString(String shapeString) {
		String[] tokens = shapeString.split(",");
		String type = tokens[0];
		int x1 = Integer.parseInt(tokens[1]);
		int y1 = Integer.parseInt(tokens[2]);
		int x2 = Integer.parseInt(tokens[3]);
		int y2 = Integer.parseInt(tokens[4]);
		Color lineColor = hexToColor(tokens[5]);
		Color fillColor = hexToColor(tokens[6]);
		int thickness = Integer.parseInt(tokens[7]);
		boolean fill = Boolean.parseBoolean(tokens[8]);
		String text = tokens[9];

		return new Shape(type, x1, y1, x2, y2, lineColor, fillColor, thickness, fill, text);
	}

	public String getType() {
		return type;
	}

	public Point getStartPoint() {
		return startPoint;
	}

	public Point getEndPoint() {
		return endPoint;
	}

	public Color getLineColor() {
		return lineColor;
	}

	public Color getFillColor() {
		return fillColor;
	}

	public int getThickness() {
		return thickness;
	}

	public boolean isFill() {
		return fill;
	}

	public String getText() {
		// 세미콜론이 포함된 경우에는 세미콜론을 삭제한 문자열을 반환
		if(text.contains(";")) {
			return text.replace(";", "");
		} else {
			return text;
		}
	}


	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}

	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}

	public void setThickness(int thickness) {
		this.thickness = thickness;
	}

	public void setFillShape(boolean fill) {
		this.fill = fill;
	}

	public boolean contains(Point p, FontMetrics fontMetrics) {
		Rectangle bounds;
		if (type.equals("line")) {
			bounds = new Rectangle(Math.min(startPoint.x, endPoint.x), Math.min(startPoint.y, endPoint.y),
					Math.abs(endPoint.x - startPoint.x), Math.abs(endPoint.y - startPoint.y));
		} else if (type.equals("circle")) {
			int radius = (int) Math.sqrt(Math.pow(endPoint.x - startPoint.x, 2) +
					Math.pow(endPoint.y - startPoint.y, 2));
			bounds = new Rectangle(startPoint.x - radius, startPoint.y - radius, radius * 2, radius * 2);
		} else if (type.equals("rectangle")) {
			int width = Math.abs(endPoint.x - startPoint.x);
			int height = Math.abs(endPoint.y - startPoint.y);
			int startX = Math.min(startPoint.x, endPoint.x);
			int startY = Math.min(startPoint.y, endPoint.y);
			bounds = new Rectangle(startX, startY, width, height);
		} else { // text
			int textWidth = fontMetrics.stringWidth(text);
			int textHeight = fontMetrics.getHeight();
			bounds = new Rectangle(startPoint.x, startPoint.y - textHeight / 2, textWidth, textHeight);
		}
		return bounds.contains(p);
	}

	@Override
	public String toString() {
		return type + "," + startPoint.x + "," + startPoint.y + "," + endPoint.x + "," + endPoint.y + ","
				+ colorToHex(lineColor) + "," + colorToHex(fillColor) + "," + thickness + ","
				+ fill + "," + text;
	}
	// Color to Hexadecimal String
	private String colorToHex(Color color) {
		if(color == null)
			return "";
		return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
	}

	// Hexadecimal String to Color
	private static Color hexToColor(String hex) {
		if(hex.isEmpty())
			return new Color(0, 0, 0, 0);

		return Color.decode(hex);
	}

}
