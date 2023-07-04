package nl.ing.lovebird.secretspipeline;

import lombok.Getter;
import org.c02e.jpgpj.Ring;

public class PGPKeyRing {

    @Getter
    private final Ring ring;

    public PGPKeyRing() {
        this.ring = new Ring();
    }

    public PGPKeyRing(Ring ring) {
        this.ring = ring;
    }
}
