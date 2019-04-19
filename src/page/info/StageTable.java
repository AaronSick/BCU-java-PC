package page.info;

import java.awt.Point;

import page.MainFrame;
import page.Page;
import page.support.AbJTable;
import page.support.EnemyTCR;
import util.stage.Stage;
import util.unit.Enemy;
import util.unit.EnemyStore;

public class StageTable extends AbJTable {

	private static final long serialVersionUID = 1L;

	private static String[] title;

	static {
		redefine();
	}

	public static void redefine() {
		title = Page.get(1, "t", 7);
	}

	protected Object[][] data;

	private final Page page;

	protected StageTable(Page p) {
		page = p;

		setDefaultRenderer(Enemy.class, new EnemyTCR(lnk));
	}

	@Override
	public Class<?> getColumnClass(int c) {
		if (c == 1)
			return Enemy.class;
		else
			return Object.class;
	}

	@Override
	public int getColumnCount() {
		return title.length;
	}

	@Override
	public String getColumnName(int arg0) {
		return title[arg0];
	}

	@Override
	public int getRowCount() {
		if (data == null)
			return 0;
		return data.length;
	}

	@Override
	public Object getValueAt(int r, int c) {
		if (data == null || r < 0 || c < 0 || r >= data.length || c >= data[r].length)
			return null;
		if (c == 2)
			return data[r][c] + "%";
		return data[r][c];
	}

	protected void clicked(Point p) {
		if (data == null)
			return;
		int c = getColumnModel().getColumnIndexAtX(p.x);
		c = lnk[c];
		int r = p.y / getRowHeight();
		if (r < 0 || r >= data.length || c != 1)
			return;
		Enemy e = (Enemy) data[r][c];
		MainFrame.changePanel(new EnemyInfoPage(page, e, (int) data[r][2]));
	}

	protected void setData(Stage st) {
		int[][] info = st.datas;
		data = new Object[info.length][7];
		for (int i = 0; i < info.length; i++) {
			int ind = info.length - i - 1;
			data[ind][1] = EnemyStore.getEnemy(info[i][0]);
			data[ind][0] = info[i][8] == 1 ? "boss" : "";
			data[ind][2] = info[i][9];
			data[ind][3] = info[i][1] == 0 ? "infinite" : info[i][1];
			data[ind][4] = info[i][5] + "%";
			data[ind][5] = info[i][2];
			if (info[i][3] == info[i][4])
				data[ind][6] = info[i][3];
			else
				data[ind][6] = info[i][3] + "~" + info[i][4];
		}
	}

}