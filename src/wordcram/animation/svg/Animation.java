package wordcram.animation.svg;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.dom4j.Element;

import wordcram.animation.svg.KeyFrame.AnimationType;

public class Animation {
	final private Element			group;
	private float					transition			= 1f;
	private float					opacityTransition	= 0.2f;
	private final float				colorTransition		= 0.2f;
	private float					timeBetweenKeyFrame	= 0f;
	private boolean					loop;

	private final List<KeyFrame>	frames;
	private final String			idPrefix;
	private Element					mainPathElement;

	private final AtomicInteger		elementIdx			= new AtomicInteger(0);
	private List<String>			pathValues;

	private String					opacity				= "0";
	private String					fill				= "";
	private float					latency				= 0;
	private float					totalTime			= 0;

	protected Animation(final String idPrefix, final Element group, final float transition,
			final float timeBetweenKeyFrame) {
		super();
		this.idPrefix = idPrefix;
		this.group = group;
		this.transition = transition;
		this.timeBetweenKeyFrame = timeBetweenKeyFrame;
		frames = new ArrayList<>();
	}

	private String currentId() {
		return idPrefix + elementIdx.get();
	}

	private String nextId() {
		return idPrefix + elementIdx.incrementAndGet();
	}

	private void resetLatency() {
		latency = 0;
	}

	private String buildTrigger(final String trigger) {
		String t = trigger + ".end";
		if (latency > 0) {
			t += "+" + latency + "s";
		}

		resetLatency();
		return t;
	}

	private List<Element> handleFrameElements(final Element path, final KeyFrame frame) {

		final List<Element> frameElements = frame.getAnimationsElement();

		final List<Element> resultingElements = new ArrayList<>();

		float duration = 0;

		final String trigger = buildTrigger(currentId());

		for (final Element element : frameElements) {

			if (element.attributeValue("attributeName")
				.equals("d")) {
				this.pathValues.add(element.attributeValue("to"));
				continue;
			}

			final String id = nextId();

			final boolean isOpacityElement = element.attributeValue("attributeName")
				.equals("opacity");

			final boolean isFillElement = element.attributeValue("attributeName")
				.equals("fill");

			duration = transition;
			if (isOpacityElement) {
				duration = opacityTransition;
			} else if (isFillElement) {
				duration = colorTransition;
			}

			final Element pathElement = path.addElement(element.getName());
			resultingElements.add(pathElement);

			pathElement.addAttribute("id", id);
			pathElement.addAttribute("begin", trigger);
			pathElement.addAttribute("dur", duration + "s");
			pathElement.addAttribute("fill", "freeze");

			System.err.println("[ANIM.: " + id + "] " + duration + "s" + " trigger:" + trigger);

			pathElement.addAttribute("attributeName", element.attributeValue("attributeName"));
			if (element.attributeValue("from") != null) {
				pathElement.addAttribute("from", element.attributeValue("from"));
			} else {
				if (isOpacityElement) {
					pathElement.addAttribute("from", opacity);
				} else if (isFillElement) {
					pathElement.addAttribute("from", fill);
				}
			}

			if (element.attributeValue("to") != null) {
				pathElement.addAttribute("to", element.attributeValue("to"));

				if (isOpacityElement) {
					opacity = element.attributeValue("to");
				} else if (isFillElement) {
					fill = element.attributeValue("to");
				}
			}

		}

		if (resultingElements.isEmpty())
			return resultingElements;

		totalTime += duration;

		if (duration < transition) {
			addLatency(transition - duration);
			System.err.println("[INTER-FRAME]");
		}

		System.err.println("[TOTAL TIME: " + totalTime + " ]");
		return resultingElements;
	}

	private void addLatency(final float time) {

		if (time <= 0)
			return;

		totalTime += time;
		latency += time;

	}

	private void initMainPathElement() {
		final Element path = group.element("path");
		this.mainPathElement = path.addElement(AnimationType.SHAPE.nodeName());

		mainPathElement.addAttribute("id", idPrefix + "_path");
		mainPathElement.addAttribute("attributeName", "d");
		mainPathElement.addAttribute("begin", "0s");

		this.pathValues = new ArrayList<>();

		if (this.loop) {
			mainPathElement.addAttribute("repeatCount", "indefinite");
		}
	}

	public String getIdPrefix() {
		return idPrefix;
	}

	public Element getGroup() {
		return group;
	}

	public float getTransition() {
		return transition;
	}

	public void setTransition(final float transition) {
		this.transition = transition;
	}

	public float getTimeBetweenKeyFrame() {
		return timeBetweenKeyFrame;
	}

	public void setTimeBetweenKeyFrame(final float timeBetweenKeyFrame) {
		this.timeBetweenKeyFrame = timeBetweenKeyFrame;
	}

	public boolean isLoop() {
		return loop;
	}

	public void setLoop(final boolean loop) {
		this.loop = loop;
	}

	public Animation addKeyFrame(final KeyFrame frame) {
		this.frames.add(frame);
		return this;
	}

	public Animation addKeyFrame(final int index, final KeyFrame frame) {
		this.frames.add(index, frame);
		return this;
	}

	public float getOpacityTransition() {
		return opacityTransition;
	}

	public void setOpacityTransition(final float opacityTransition) {
		this.opacityTransition = opacityTransition;
	}

	public void compile() {

		// final int[] elementIdx = new int[] { 0 };

		initMainPathElement();
		final List<Element> initElements = new ArrayList<>();

		final boolean[] isFirstFrame = new boolean[] { true };

		int frameNum = 1;

		final Element path = group.element("path");

		if (path.attribute("fill") != null) {
			fill = path.attributeValue("fill");
		} else {
			fill = "#000000";
		}

		final int maxElementPerFrame = frames.stream()
			.mapToInt(KeyFrame::size)
			.max()
			.getAsInt();

		for (final KeyFrame frame : frames) {
			System.err.println("---------------------");
			System.err.println("FRAME " + frameNum);
			frameNum++;

			if (elementIdx.get() > 0 && timeBetweenKeyFrame > 0) {
				addLatency(timeBetweenKeyFrame);
			}

			final List<Element> resultingElements = handleFrameElements(path, frame);

			if (isFirstFrame[0]) {
				initElements.addAll(resultingElements);
			}

			isFirstFrame[0] = false;
			// resetLatency();
		}

		System.err.println("Set path values");
		final StringBuilder builder = new StringBuilder();
		boolean isFirst = true;

		if (this.loop) {
			pathValues.add(pathValues.get(0));
		}

		for (final String value : pathValues) {
			if (!isFirst) {
				builder.append(";");
			}

			builder.append("\n" + value);
			isFirst = false;
		}

		mainPathElement.addAttribute("dur", totalTime + "s");
		mainPathElement.addAttribute("values", builder.toString());

		String begin = "0s";
		if (this.loop) {
			begin += ";" + buildTrigger(currentId());
		}

		System.err.println("Set Begin to first frame: " + begin);
		for (final Element initElement : initElements) {
			initElement.addAttribute("begin", begin);
			// initElement.addAttribute("dur", "1ms");
		}
	}
}
