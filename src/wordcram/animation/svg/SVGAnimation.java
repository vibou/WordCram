package wordcram.animation.svg;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.dom4j.Element;

import wordcram.animation.svg.KeyFrame.AnimationType;

public class SVGAnimation {
	final private Element			group;
	private float					transition			= 1f;
	private float					opacityTransition	= 1f;
	private float					colorTransition		= 1f;
	private float					timeBetweenKeyFrame	= 2.0f;
	private boolean					loop;

	private final List<KeyFrame>	frames;
	private final String			idPrefix;
	private Element					mainPathElement;

	private final AtomicInteger		elementIdx			= new AtomicInteger(0);

	private List<String[]>			pathKeyValues;

	private String					opacity				= "0";
	private String					fill				= "";
	private String					path;

	private float					latency				= 0;
	private float					totalTime			= 0;
	private final boolean			isDebug				= true;

	protected SVGAnimation(final String idPrefix, final Element group) {
		super();
		this.idPrefix = idPrefix;
		this.group = group;
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
		final String t = buildAdvancedTrigger(trigger, latency, "+");
		resetLatency();
		return t;
	}

	private String buildAdvancedTrigger(final String trigger, final float latency,
			final String symbol) {
		String t = trigger + ".end";
		if (latency > 0) {
			t += symbol + String.format(Locale.ENGLISH, "%.1f", latency) + "s";
		}

		return t;
	}

	private List<Element> handleFrameElements(final Element path, final KeyFrame frame,
			final int frameIdx, final boolean lastFrame) {

		final List<Element> frameElements = frame.getAnimationsElement();

		final List<Element> resultingElements = new ArrayList<>();

		float duration = 0;

		if (!pathKeyValues.isEmpty()) {
			final String[] lastPath = pathKeyValues.get(pathKeyValues.size() - 1);
			final String currentTime = String.format(Locale.ENGLISH, "%.1f", totalTime);
			if (!lastPath[0].equals(currentTime)) {
				addToPathAnimation(lastPath[1]);
			}
		}

		final String trigger = buildTrigger(currentId());

		float frameDuration = timeBetweenKeyFrame + transition;

		if (lastFrame) {
			frameDuration = transition;
		}

		boolean newPositionAdded = false;
		for (final Element element : frameElements) {

			final String attributeName = element.attributeValue("attributeName");

			boolean isOpacity = false;
			boolean isFill = false;

			if (attributeName.equals("d")) {

				if (!newPositionAdded) {

					this.path = element.attributeValue("to");
					newPositionAdded = true;
				}

				continue;
			} else if (attributeName.equals("opacity")) {
				isOpacity = true;
			} else if (attributeName.equals("fill")) {
				isFill = true;
			}

			duration = transition;
			if (isOpacity) {
				duration = opacityTransition;
			} else if (isFill) {
				duration = colorTransition;
			}

			String from = "";
			String to = "";

			if (element.attributeValue("from") != null) {
				from = element.attributeValue("from");
			} else {
				if (isOpacity) {
					from = opacity;
				} else if (isFill) {
					from = fill;
				}
			}

			if (element.attributeValue("to") != null) {
				to = element.attributeValue("to");
			}

			if (isOpacity) {
				opacity = element.attributeValue("to");
			} else if (isFill) {
				fill = element.attributeValue("to");
			}

			// if ((frameIdx > 1 || timeBetweenKeyFrame == 0) &&
			// from.equals(to)) {
			// continue;
			// }

			final String id = nextId();
			final Element pathElement = path.addElement(element.getName());
			resultingElements.add(pathElement);
			pathElement.addAttribute("id", id);
			pathElement.addAttribute("class", "frame" + frameIdx);
			pathElement.addAttribute("begin", trigger);
			pathElement.addAttribute("dur", duration + "s");
			pathElement.addAttribute("fill", "freeze");
			pathElement.addAttribute("from", from);
			pathElement.addAttribute("to", to);

			pathElement.addAttribute("attributeName", element.attributeValue("attributeName"));

			debug("[ANIM.: " + id + " " + element.attributeValue("attributeName") + "] dur: "
					+ duration + "s; trigger:" + trigger + "; from: " + from + "; to: " + to);
		}

		if (resultingElements.isEmpty())
			return resultingElements;

		totalTime += duration;

		if (duration < transition) {
			addLatency(transition - duration);
			debug("[INTER-FRAME " + latency + "s]");

			duration += transition - duration;
		}

		addToPathAnimation(this.path);

		if (duration < frameDuration) {
			addLatency(frameDuration - duration);
			addToPathAnimation(this.path);
		}

		debug("[TOTAL TIME: " + totalTime + " ]");
		return resultingElements;
	}

	private void addToPathAnimation(final String path) {
		debug("[PATH ADD VALUE at time " + totalTime + "s ]");
		this.pathKeyValues.add(new String[] { String.format(Locale.ENGLISH, "%.1f", totalTime),
				path });
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

		debug("Set path values");
		final StringBuilder keys = new StringBuilder();
		final StringBuilder values = new StringBuilder();

		boolean isFirst = true;
		float previousKey = -1;
		for (final String[] tuple : pathKeyValues) {
			if (!isFirst) {
				keys.append(";");
				values.append(";");
			}

			float key = Float.parseFloat(tuple[0]) / totalTime;

			while (previousKey >= key) {
				key += 0.01f;
			}

			previousKey = key;

			keys.append(String.format(Locale.ENGLISH, "%#.2f", key));
			values.append(tuple[1]);
			isFirst = false;
		}
		mainPathElement.addAttribute("dur", totalTime + "s");
		mainPathElement.addAttribute("keyTimes", keys.toString());
		mainPathElement.addAttribute("values", values.toString());

		mainPathElement.addAttribute("id", idPrefix + "_path");
		mainPathElement.addAttribute("attributeName", "d");
		mainPathElement.addAttribute("begin", "0s");

		if (this.loop) {
			mainPathElement.addAttribute("repeatCount", "indefinite");
		}

		debug("Number of path-values: " + pathKeyValues.size());
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

	public SVGAnimation addKeyFrame(final KeyFrame frame) {
		this.frames.add(frame);
		return this;
	}

	public SVGAnimation addKeyFrame(final int index, final KeyFrame frame) {
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

		opacityTransition = Math.min(opacityTransition, transition);
		colorTransition = Math.min(colorTransition, transition);

		this.pathKeyValues = new ArrayList<>();

		final List<Element> initElements = new ArrayList<>();

		final boolean[] isFirstFrame = new boolean[] { true };

		final Element path = group.element("path");

		this.path = path.attributeValue("d");

		if (path.attribute("fill") != null) {
			fill = path.attributeValue("fill");
		} else {
			fill = "#000000";
		}

		if (path.attribute("opacity") != null) {
			opacity = path.attributeValue("opacity");
		} else {
			opacity = "1";
		}

		debug("[ADD FIRST PATH]");
		addToPathAnimation(path.attributeValue("d"));

		if (this.loop) {
			// To get back to initial state
			frames.add(frames.get(0));
		}

		if (timeBetweenKeyFrame > 0) {
			debug("[ADD FIRST LATENCY]");
			addLatency(timeBetweenKeyFrame);

			if (this.loop) {
				addToPathAnimation(path.attributeValue("d"));
			}

			debug("[TOTAL TIME: " + latency + "s ]");
		}

		// is initial state
		frames.remove(0);

		int frameNum = 1;
		final int nbFrame = frames.size();
		for (final KeyFrame frame : frames) {
			debug("---------------------");
			debug("FRAME " + frameNum);

			final boolean lastFrame = nbFrame == frameNum;
			final List<Element> resultingElements = handleFrameElements(path, frame, frameNum,
					lastFrame);

			if (isFirstFrame[0]) {
				initElements.addAll(resultingElements);
			}

			isFirstFrame[0] = false;
			frameNum++;
			//
		}

		if (latency > 0) {
			final Element pathElement = path.addElement(AnimationType.SHAPE.nodeName());
			final float duration = latency;
			final String trigger = currentId();
			final String id = nextId();
			pathElement.addAttribute("id", id);
			pathElement.addAttribute("class", "extra-frame");

			resetLatency();

			pathElement.addAttribute("begin", buildTrigger(trigger));
			pathElement.addAttribute("dur", String.format(Locale.ENGLISH, "%.1fs", duration));
			pathElement.addAttribute("fill", "freeze");
			pathElement.addAttribute("from", opacity);
			pathElement.addAttribute("to", opacity);

			pathElement.addAttribute("attributeName", "opacity");

			debug("[EXTRA-ANIM.: " + id + " ] dur: " + duration + "s; trigger:" + trigger
					+ "; from: " + opacity + "; to: " + opacity);

		}

		initMainPathElement();

		resetLatency();
		if (this.loop) {
			addLatency(timeBetweenKeyFrame);
		} else {
			addLatency(timeBetweenKeyFrame);
		}
		String begin = String.format(Locale.ENGLISH, "%.1fs", timeBetweenKeyFrame);
		if (this.loop) {
			begin += ";" + buildTrigger(currentId());
		}

		debug("Set Begin to first frame: " + begin);
		for (final Element initElement : initElements) {
			initElement.addAttribute("begin", begin);
			// initElement.addAttribute("dur", "1ms");
		}
	}

	private void debug(final String string) {
		if (isDebug) {
			System.err.println(string);
		}
	}

	public void setColorTransition(final float colorTransition) {
		this.colorTransition = colorTransition;
	}
}
