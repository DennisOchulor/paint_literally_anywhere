package io.github.dennisochulor.paint_literally_anywhere.shape;

import java.util.BitSet;

public record QuadInstance(
        QuadTemplate template,
        int rows,
        int cols,
        int[] pixels,
        BitSet emissiveData
) {

}
