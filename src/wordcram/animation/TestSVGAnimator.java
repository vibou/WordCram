package wordcram.animation;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;

import wordcram.animation.AnimatorFactory.Animators;

public class TestSVGAnimator {

	@Test
	public void test() {
		final File folder = new File("svgs");

		final IAnimator animator = AnimatorFactory.createAnimator(Animators.SVGAnimator);

		try {
			animator.addFolder(folder)
				.setLoop(true)
				.toFile(new File("animatedSVG.svg"));
		} catch (final Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
