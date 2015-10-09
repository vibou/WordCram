package wordcram.animation.svg;

import java.util.stream.Stream;

public class AttributeStream {

	public static AttributeStream open(final String attributeName) {
		return new AttributeStream("attributeName", attributeName);
	}

	private Stream<Attribute>	attributeStream;

	protected AttributeStream(final String key, final String value) {
		final Attribute attr = new Attribute(key, value);
		attributeStream = Stream.of(attr);
	}

	public AttributeStream add(final String key, final float value) {
		add(key, Float.toString(value));
		return this;
	}

	public AttributeStream add(final String key, final Object value) {
		final Attribute attr = new Attribute(key, value.toString());
		attributeStream = Stream.concat(attributeStream, Stream.of(attr));
		return this;
	}

	public Attribute[] close() {
		return attributeStream.toArray(i -> new Attribute[i]);
	}
}
