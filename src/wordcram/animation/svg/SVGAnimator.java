package wordcram.animation.svg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import wordcram.animation.IAnimator;
import wordcram.animation.svg.KeyFrame.AnimationType;

public class SVGAnimator implements IAnimator {

	private static FilenameFilter			filter				= new FilenameFilter() {

																	@Override
																	public boolean accept(
																			final File arg0,
																			final String arg1) {
																		return arg1.endsWith("svg");
																	}

																};

	private final List<File>				files				= new ArrayList<File>();

	private float							transition			= 0.5f;
	private final float						opacityTransition	= 0.2f;
	private final float						colorTransition		= 0.2f;
	private float							timeBetweenKeyFrame	= 2.0f;
	private boolean							loop				= false;

	private HashMap<String, List<WordInfo>>	wordInfoMap;

	private HashMap<String, String>			pathMap;

	private XMLContentHandler				handler;

	public SVGAnimator() {

	}

	@Override
	public IAnimator setTransition(final float f) {
		this.transition = (f <= 0) ? 0.5f : f;
		return this;
	}

	@Override
	public IAnimator setDelayBetweenFiles(final float seconds) {
		this.timeBetweenKeyFrame = (seconds < 0) ? 0 : seconds;
		return this;
	}

	@Override
	public IAnimator addFile(final File file) throws IOException {
		files.add(file);
		return this;
	}

	@Override
	public IAnimator addFolder(final File folder) throws IOException {
		if (folder == null || !folder.exists() || !folder.isDirectory())
			throw new IOException("The folder "
					+ ((folder == null) ? "null" : folder.getAbsolutePath())
					+ " does not exist or is not a directory");

		for (final File f : folder.listFiles()) {
			if (!filter.accept(f.getParentFile(), f.getName())) {
				continue;
			}

			files.add(f);
		}
		return this;
	}

	@Override
	public void toFile(final File svgFile) throws IOException {

		final Document doc = DocumentHelper.createDocument();
		doc.addDocType("svg", "-//W3C//DTD SVG 1.0//EN",
				"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd");

		final Element root = doc.addElement("svg");
		root.addNamespace("", "http://www.w3.org/2000/svg");
		root.addNamespace("xlink", "http://www.w3.org/1999/xlink");

		readFiles();

		wordInfoMap = handler.getWordInfoMap();
		pathMap = handler.getPathMap();

		final BufferedWriter writer = new BufferedWriter(new FileWriter(svgFile));

		for (final String word : wordInfoMap.keySet()) {

			if (!(word.equals("Wakefern") || word.equals("O"))) {
				continue;
			}

			final List<WordInfo> infos = wordInfoMap.get(word);

			if (infos.isEmpty()) {
				continue;
			}

			final String pathStr = pathMap.get(word);

			final Element group = root.addElement("g", "http://www.w3.org/2000/svg");
			group.addAttribute("id", word);
			final Element path = group.addElement("path");
			path.addAttribute("d", pathStr);

			final SVGAnimation a = new SVGAnimation(word, group);

			boolean isFirst = true;
			KeyFrame firstFrame = null;
			for (final WordInfo info : infos) {

				if (isFirst) {
					path.addAttribute("fill", info.getStyleAsString("fill"));
					path.addAttribute("opacity", Float.toString(info.getOpacity()));
					isFirst = false;

				}

				final KeyFrame frame = new KeyFrame(info.getPath());
				if (firstFrame == null) {
					firstFrame = frame;
				}

				a.addKeyFrame(frame);

				frame.addTransition(AnimationType.SHAPE, AttributeStream.open("opacity")
					.add("to", info.getOpacity())
					.close());

				frame.addTransition(AnimationType.SHAPE, AttributeStream.open("d")
					.add("to", info.getPath())
					.close());

				frame.addTransition(AnimationType.COLOR, AttributeStream.open("fill")
					.add("to", info.getStyleAsString("fill"))
					.close());
			}

			a.setTransition(transition);
			a.setTimeBetweenKeyFrame(timeBetweenKeyFrame);
			a.setLoop(loop);

			a.compile();
		}

		writer.write("<" + doc.asXML()
			.substring(1)
			.replaceAll("<", "\n<"));
		writer.close();
	}

	private void readFiles() {
		try {
			_readFiles();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private void _readFiles() throws Exception {

		final SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(false);
		factory.setValidating(false);
		factory.setFeature("http://xml.org/sax/features/namespaces", false);
		factory.setFeature("http://xml.org/sax/features/validation", false);
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

		SAXParser parser = null;

		try {
			parser = factory.newSAXParser();
		} catch (final Exception e1) {
			e1.printStackTrace();
			return;
		}

		handler = new XMLContentHandler();

		for (final File f : files) {
			try {
				parser.parse(f, handler);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see wordcram.animation.IAnimator#setLoop(boolean)
	 */
	@Override
	public IAnimator setLoop(final boolean bool) {
		this.loop = bool;
		return this;
	}

	public float getTransition() {
		return transition;
	}

	public float getOpacityTransition() {
		return opacityTransition;
	}

	public float getColorTransition() {
		return colorTransition;
	}

	public boolean isLoop() {
		return loop;
	}

}
