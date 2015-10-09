package wordcram.animation.svg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.tree.BaseElement;

public class KeyFrame {

	public static enum AnimationType {
		SHAPE("animate"),
		COLOR("animateColor");

		private String	nodeName;

		private AnimationType(final String nodeName) {
			this.nodeName = nodeName;
		}

		public String nodeName() {
			return nodeName;
		}

	};

	private final List<Element>	animationsElement;

	public KeyFrame(final String path) {
		super();

		this.animationsElement = new ArrayList<>();
		this.addTransition(AnimationType.SHAPE, AttributeStream.open("d")
			.add("to", path)
			.close());
	}

	public List<Element> getAnimationsElement() {
		return Collections.unmodifiableList(animationsElement);
	}

	public void addTransition(final AnimationType type, final Attribute[] attributes) {
		final Element element = new BaseElement(type.nodeName);
		for (final Attribute attr : attributes) {
			element.addAttribute(attr.getKey(), attr.getValue());
		}

		animationsElement.add(element);
	}

	public int size() {
		return animationsElement.size();
	}
}
