package util.anim;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import javax.imageio.ImageIO;

import io.InStream;
import io.OutStream;
import io.Writer;
import main.MainBCU;
import main.Printer;
import page.anim.EditHead;
import util.system.VFile;
import util.system.VImg;

public class AnimC extends AnimU {

	public static String getAvailable(String str) {
		File folder = new File("./res/anim/");
		if (!folder.exists())
			return str;
		File[] fs = new File("./res/anim/").listFiles();
		Set<String> strs = new HashSet<>();
		for (int i = 0; i < fs.length; i++)
			strs.add(fs[i].getName());
		while (strs.contains(str))
			str += "'";
		return str;
	}

	private boolean saved = false;
	public boolean inPool;
	public EditHead link;
	public Stack<OutStream> history = new Stack<>();
	public String name = "";
	public String prev;

	public AnimC(InStream is) {
		name = "local animation";
		inPool = false;
		loaded = true;
		partial = true;
		saved = true;
		ByteArrayInputStream bais = new ByteArrayInputStream(is.nextBytesI());
		try {
			num = ImageIO.read(bais);
		} catch (IOException e) {
			e.printStackTrace();
		}
		imgcut = ImgCut.newIns(new VFile(null, "", is.nextBytesI()));
		mamodel = MaModel.newIns(new VFile(null, "", is.nextBytesI()));
		int n = is.nextInt();
		anims = new MaAnim[n];
		for (int i = 0; i < n; i++)
			anims[i] = MaAnim.newIns(new VFile(null, "", is.nextBytesI()));
		parts = imgcut.cut(num);
		if (!is.end()) {
			bais = new ByteArrayInputStream(is.nextBytesI());
			try {
				VImg vimg = new VImg(ImageIO.read(bais));
				if (vimg.getImg().getHeight() == 32)
					edi = vimg;
				else
					uni = vimg;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (!is.end()) {
			bais = new ByteArrayInputStream(is.nextBytesI());
			try {
				uni = new VImg(ImageIO.read(bais));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public AnimC(String st) {
		inPool = true;
		prev = "./res/anim/";
		name = st;
		VFile f = VFile.getFile(prev + name + "/edi.png");
		if (f != null)
			edi = new VImg(f);
		f = VFile.getFile(prev + name + "/uni.png");
		if (f != null)
			uni = new VImg(f);
	}

	public AnimC(String str, AnimU ori) {
		inPool = true;
		prev = "./res/anim/";
		name = str;
		loaded = true;
		partial = true;
		imgcut = ori.imgcut.clone();
		mamodel = ori.mamodel.clone();
		anims = new MaAnim[7];
		for (int i = 0; i < 7; i++)
			if (i < ori.anims.length)
				anims[i] = ori.anims[i].clone();
			else
				anims[i] = new MaAnim();
		num = ori.num;
		parts = imgcut.cut(num);
		File f = new File(prev + name + "/" + name + ".png");
		Writer.check(f);
		try {
			ImageIO.write(num, "PNG", f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		reloImg();
		edi = ori.edi;
		saveIcon();
		uni = ori.uni;
		saveUni();
		history();
	}

	public void delete() {
		Writer.delete(new File(prev + name + "/"));
	}

	public void hardSave(String str) {
		if (prev == null)
			prev = "./res/anim/";
		if (name == null)
			name = AnimC.getAvailable(MainBCU.validate(str));
		saved = false;
		save();
		saveImg();
		saveIcon();
		saveUni();
	}

	public void ICedited() {
		check();
		parts = imgcut.cut(num);
	}

	public boolean isSaved() {
		return saved;
	}

	@Override
	public void load() {
		loaded = true;
		try {
			String pre = prev + name + "/" + name;
			num = read(VFile.getFile(pre + ".png"));
			imgcut = ImgCut.newIns(pre + ".imgcut");
			if (num == null) {
				Printer.e("AnimC", 147, "can't read png: " + pre);
				MainBCU.pop("sprite missing: " + pre + ".png", "loading error");
				Writer.logClose(false);
				System.exit(0);
			}
			parts = imgcut.cut(num);
			partial();
			if (edi != null)
				edi.check();
			if (uni != null)
				uni.check();
			history();
		} catch (Exception e) {
			MainBCU.pop("Error in loading custom animation: " + name, "loading error");
			e.printStackTrace();
			System.exit(0);
		}
		validate();
	}

	public void reloImg() {
		num = read(VFile.getFile(prev + name + "/" + name + ".png"));
	}

	public void renameTo(String str) {
		if (uni != null)
			uni.check();
		if (edi != null)
			edi.check();
		Writer.delete(new File(prev + name + "/"));
		name = str;
		saveImg();
		saveIcon();
		saveUni();
		unSave();
	}

	public void restore() {
		history.pop();
		InStream is = history.peek().translate();
		imgcut.restore(is);
		ICedited();
		mamodel.restore(is);
		int n = is.nextInt();
		anims = new MaAnim[n];
		for (int i = 0; i < n; i++) {
			anims[i] = new MaAnim();
			anims[i].restore(is);
		}
		saved = false;
	}

	@Override
	public void revert() {
		super.revert();
		unSave();
	}

	public void save() {
		if (!loaded || isSaved() || mismatch)
			return;
		saved = true;
		String pre = prev + name + "/" + name;
		save$g(pre, 0, 0);
		save$g(pre, 1, 0);

		for (int i = 0; i < 4; i++)
			save$g(pre + "0" + i, 2, i);
		if (anims.length == 7)
			for (int i = 0; i < 3; i++)
				save$g(pre + "_zombie0" + i, 2, i + 4);
	}

	public void saveIcon() {
		if (edi == null || edi.getImg() == null)
			return;
		try {
			File f = new File(prev + name + "/edi.png");
			Writer.check(f);
			ImageIO.write(edi.getImg(), "PNG", f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveImg() {
		try {
			File f = new File(prev + name + "/" + name + ".png");
			Writer.check(f);
			if (!ImageIO.write(num, "PNG", f))
				MainBCU.warning("failed to write sprite", "IO error");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveUni() {
		if (uni == null || uni.getImg() == null)
			return;
		try {
			File f = new File(prev + name + "/uni.png");
			Writer.check(f);
			ImageIO.write(uni.getImg(), "PNG", f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return name;
	}

	public void unSave() {
		saved = false;
		history();
		if (link != null)
			link.review();
	}

	public OutStream write() {
		OutStream osi = new OutStream();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(num, "PNG", baos);
		} catch (IOException e1) {
			e1.printStackTrace();
			osi.terminate();
			return osi;
		}
		osi.writeBytesI(baos.toByteArray());
		try {
			baos = new ByteArrayOutputStream();
			imgcut.write(new PrintStream(baos, true, "UTF-8"));
			osi.writeBytesI(baos.toByteArray());
			baos = new ByteArrayOutputStream();
			mamodel.write(new PrintStream(baos, true, "UTF-8"));
			osi.writeBytesI(baos.toByteArray());
			osi.writeInt(anims.length);
			for (MaAnim ani : anims) {
				baos = new ByteArrayOutputStream();
				ani.write(new PrintStream(baos, true, "UTF-8"));
				osi.writeBytesI(baos.toByteArray());
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (edi != null && edi.getImg() != null) {
			baos = new ByteArrayOutputStream();
			try {
				ImageIO.write(edi.getImg(), "PNG", baos);
			} catch (IOException e1) {
				e1.printStackTrace();
				osi.terminate();
				return osi;
			}
			osi.writeBytesI(baos.toByteArray());
		}
		if (uni != null && uni.getImg() != null) {
			baos = new ByteArrayOutputStream();
			try {
				ImageIO.write(uni.getImg(), "PNG", baos);
			} catch (IOException e1) {
				e1.printStackTrace();
				osi.terminate();
				return osi;
			}
			osi.writeBytesI(baos.toByteArray());
		}
		osi.terminate();
		return osi;
	}

	@Override
	protected void partial() {
		if (!partial) {
			partial = true;
			String pre = prev + name + "/" + name;
			mamodel = MaModel.newIns(pre + ".mamodel");
			anims = new MaAnim[7];
			for (int i = 0; i < 4; i++)
				anims[i] = MaAnim.newIns(pre + "0" + i + ".maanim");
			for (int i = 0; i < 3; i++)
				anims[i + 4] = MaAnim.newIns(pre + "_zombie0" + i + ".maanim");
		}
	}

	private void history() {
		OutStream os = new OutStream();
		imgcut.write(os);
		mamodel.write(os);
		os.writeInt(anims.length);
		for (MaAnim ma : anims)
			ma.write(os);
		os.terminate();
		history.push(os);
	}

	private void save$g(String pre, int type, int para) {
		try {
			save$s(pre, type, para);
		} catch (Exception e1) {
			e1.printStackTrace();
			try {
				save$s(pre, type, para);
			} catch (Exception e2) {
				e2.printStackTrace();
				String str = type == 0 ? ".imgcut" : type == 1 ? ".mamodel" : ".maanim";
				MainBCU.pop("cannot save " + pre + str, "IO error");
			}
		}
	}

	private void save$ic(String pre) throws Exception {
		File f = new File(pre + ".imgcut");
		Writer.check(f);
		PrintStream ps = new PrintStream(f, "UTF-8");
		imgcut.write(ps);
		ps.close();
		new ImgCut(readLine(new VFile(f)));
	}

	private void save$ma(String pre, int i) throws Exception {
		File f = new File(pre + ".maanim");
		Writer.check(f);
		PrintStream ps = new PrintStream(f, "UTF-8");
		anims[i].write(ps);
		ps.close();
		new MaAnim(readLine(new VFile(f)));
	}

	private void save$mm(String pre) throws Exception {
		File f = new File(pre + ".mamodel");
		Writer.check(f);
		PrintStream ps = new PrintStream(f, "UTF-8");
		mamodel.write(ps);
		ps.close();
		new MaModel(readLine(new VFile(f)));
	}

	private void save$s(String pre, int type, int para) throws Exception {
		if (type == 0)
			save$ic(pre);
		if (type == 1)
			save$mm(pre);
		if (type == 2)
			save$ma(pre, para);
	}

}
