package decode;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;

import io.Writer;
import main.Opts;
import page.LoadPage;
import util.system.files.AssetData;
import util.system.files.VFile;

public class ZipLib {

	public static final String[] LIBREQS = { "000001", "000002", "000003" };
	public static final String[] OPTREQS = { "080504" };

	public static FileSystem lib;
	public static LibInfo info;

	public static void check() {
		for (String req : LIBREQS)
			if (!info.merge.set.contains(req)) {
				Opts.loadErr("this version requires lib " + req);
				Writer.logClose(false);
				System.exit(0);
			}
	}

	public static void init() {
		LoadPage.prog("finding library...");
		// ZipAdmin.fakeLoad();if(true)return;

		File f = new File("./assets/assets.zip");
		if (!f.exists())
			return;
		try {
			lib = FileSystems.newFileSystem(f.toPath(), null);
			info = new LibInfo(lib);
		} catch (IOException e) {
			e.printStackTrace();
			Opts.loadErr("cannot access ./assets/assets.zip");
			Writer.logClose(false);
			System.exit(0);
		}
	}

	public static void merge(File f) {
		try {
			FileSystem temp = FileSystems.newFileSystem(f.toPath(), null);
			LibInfo nlib = new LibInfo(temp);
			info.merge(nlib);
			temp.close();
			f.delete();
		} catch (IOException e) {
			Opts.loadErr("failed to merge lib");
			e.printStackTrace();
		}
	}

	public static void read() {
		LoadPage.prog("reading assets...");
		try {
			int i = 0;
			int tot = info.merge.paths.size();
			for (PathInfo pi : info.merge.paths.values()) {
				if (pi.type != 0)
					continue;
				byte[] data = Files.readAllBytes(lib.getPath(pi.path));
				VFile.root.build(pi.path, AssetData.getAsset(data));
				LoadPage.prog("reading assets " + i++ + "/" + tot);
			}
			VFile.root.sort();
			VFile.root.getIf(p -> {
				if (p.list() == null)
					return false;
				for (VFile<AssetData> v : p.list())
					if (!v.getName().startsWith("__LANG_"))
						return false;
				return true;
			}).forEach(p -> p.replace(AssetData.getAsset(p)));
		} catch (IOException e) {
			Opts.loadErr("failed to access library");
			e.printStackTrace();
		}
	}

}
