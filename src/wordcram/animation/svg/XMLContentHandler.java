package wordcram.animation.svg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLContentHandler extends DefaultHandler {

	private int										fileIdx		= 0;

	private final HashMap<String, List<WordInfo>>	wordInfoMap	= new HashMap<String, List<WordInfo>>();
	private final HashMap<String, String>			pathMap		= new HashMap<String, String>();

	private String									lastId		= "";
	private List<WordInfo>							lastInfos;

	@Override
	public void characters(final char[] ch, final int start, final int length) throws SAXException {

	}

	@Override
	public void endDocument() throws SAXException {

		for (final String word : wordInfoMap.keySet()) {
			final List<WordInfo> infos = wordInfoMap.get(word);
			final WordInfo info = infos.get(infos.size() - 1);
			while (infos.size() < fileIdx) {
				final WordInfo clone = info.copy();
				clone.setOpacity(0);
				infos.add(clone);
				lastInfos.add(clone);
			}
		}

	}

	@Override
	public void endElement(final String uri, final String localName, final String qName)
			throws SAXException {

	}

	@Override
	public void endPrefixMapping(final String prefix) throws SAXException {

	}

	@Override
	public void ignorableWhitespace(final char[] ch, final int start, final int length)
			throws SAXException {

	}

	@Override
	public void processingInstruction(final String target, final String data) throws SAXException {

	}

	@Override
	public void setDocumentLocator(final Locator locator) {

	}

	@Override
	public void skippedEntity(final String name) throws SAXException {

	}

	@Override
	public void startDocument() throws SAXException {
		fileIdx++;
		lastId = "";
		if (lastInfos == null) {
			lastInfos = new ArrayList<WordInfo>();
		} else {
			lastInfos.clear();
		}

	}

	@Override
	public void startElement(final String uri, final String localName, final String qName,
			final Attributes atts) throws SAXException {

		if (qName == "g") {
			lastId = atts.getValue("id");
			final List<WordInfo> infos = wordInfoMap.computeIfAbsent(lastId,
					id -> new ArrayList<WordInfo>());

			final WordInfo info = new WordInfo(lastId);
			info.addStyle(atts);

			while (infos.size() < (fileIdx - 1)) {
				final WordInfo clone = info.copy();
				clone.setOpacity(0);
				infos.add(clone);
				lastInfos.add(clone);
			}

			lastInfos.add(info);
			infos.add(info);
		} else if (qName == "path") {
			final String path = atts.getValue("d");
			if (!pathMap.containsKey(lastId)) {
				pathMap.put(lastId, path);
			}

			for (final WordInfo info : lastInfos) {
				info.setPath(path);
			}

			lastInfos.clear();
		}

	}

	@Override
	public void startPrefixMapping(final String prefix, final String uri) throws SAXException {

	}

	public final HashMap<String, List<WordInfo>> getWordInfoMap() {
		return wordInfoMap;
	}

	public final HashMap<String, String> getPathMap() {
		return pathMap;
	}
}
